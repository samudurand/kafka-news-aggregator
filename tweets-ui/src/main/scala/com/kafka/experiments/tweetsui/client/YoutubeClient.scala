package com.kafka.experiments.tweetsui.client

import cats.effect.IO
import com.kafka.experiments.tweetsui.config.YoutubeConfig
import com.typesafe.scalalogging.StrictLogging
import io.circe.Json
import org.http4s.Method.GET
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.implicits.http4sLiteralsSyntax

import java.time.temporal.ChronoUnit
import scala.concurrent.duration.{Duration, SECONDS}

case class VideoMetadata(
    id: String,
    dislikeCount: Long,
    duration: Duration,
    favouriteCount: Long,
    likeCount: Long,
    viewCount: Long
)

trait YoutubeClient {
  def videoData(videoId: String): IO[Option[VideoMetadata]]
}

object YoutubeClient {
  def apply(config: YoutubeConfig, httpClient: Client[IO]): YoutubeClient = new DefaultYoutubeClient(config, httpClient)
}

class DefaultYoutubeClient(config: YoutubeConfig, httpClient: Client[IO])
    extends YoutubeClient
    with Http4sClientDsl[IO]
    with StrictLogging {

  private val videosUri: Uri = Uri.fromString(s"${config.baseUrl}/videos").getOrElse(uri"")

  override def videoData(videoId: String): IO[Option[VideoMetadata]] = {
    val videoMetadataUrl = buildVideoMetadataUrl(videoId)
    val request = GET(videoMetadataUrl)

    logger.info(s"New video metadata query with request: $request")
    httpClient
      .expect[String](request)
      .map(response => parseResponse(videoId, response))
  }

  private def parseResponse(id: String, response: String): Option[VideoMetadata] = {
    import io.circe.parser._

    parse(response) match {
      case Left(parsingError) =>
        throw new RuntimeException("Unable to parse Youtube Video metadata response", parsingError)
      case Right(json) =>
        json.hcursor.downField("items").as[List[Json]] match {
          case Right(items) =>
            parseMetadata(id, items)
          case Left(error) =>
            logger.error(s"Error while trying to parse Youtube API response", error)
            None
        }
    }
  }

  private def parseMetadata(id: String, items: List[Json]) = {
    items.headOption.flatMap(metadata => {
      (for {
        videoDuration <- metadata.hcursor.downField("contentDetails").downField("duration").as[String]
        statistics = metadata.hcursor.downField("statistics")
        dislikeCount <- statistics.downField("dislikeCount").as[Long]
        favoriteCount <- statistics.downField("favoriteCount").as[Long]
        likeCount <- statistics.downField("likeCount").as[Long]
        viewCount <- statistics.downField("viewCount").as[Long]
      } yield VideoMetadata(
        id,
        dislikeCount,
        iso8601DurationToMinutes(videoDuration),
        favoriteCount,
        likeCount,
        viewCount
      )) match {
        case Right(data) => Some(data)
        case Left(error) =>
          logger.error(s"Error while trying to parse Youtube API response", error)
          None
      }
    })
  }

  private def buildVideoMetadataUrl(videoId: String) = {
    videosUri
      .withQueryParam("id", videoId)
      .withQueryParam("part", List("statistics", "contentDetails"))
      .withQueryParam("key", config.apiKey)
  }

  private def iso8601DurationToMinutes(duration: String): Duration = {
    Duration(java.time.Duration.parse(duration).get(ChronoUnit.SECONDS), SECONDS)
  }
}
