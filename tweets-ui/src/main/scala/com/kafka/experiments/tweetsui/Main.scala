package com.kafka.experiments.tweetsui

import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import com.kafka.experiments.shared.{ArticleTweet, AudioTweet, ExcludedTweet, InterestingTweet, VersionReleaseTweet, VideoTweet}
import com.kafka.experiments.tweetsui.Encoders._
import com.kafka.experiments.tweetsui.ReportBuilder.generateReport
import com.kafka.experiments.tweetsui.config.GlobalConfig
import com.typesafe.scalalogging.StrictLogging
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.staticcontent.{ResourceService, resourceService}
import org.http4s.server.{Router, Server}
import org.http4s.{Header, HttpRoutes}
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContext.global

object CountResult {
  implicit val codec: Codec[CountResult] = deriveCodec
}
case class CountResult(count: Long)

object SourceCategoryQueryParamMatcher extends QueryParamDecoderMatcher[String]("source")
object TargetCategoryQueryParamMatcher extends QueryParamDecoderMatcher[String]("target")

object Main extends IOApp with StrictLogging {

  private val config = ConfigSource.default.loadOrThrow[GlobalConfig]

  private val mongoService = MongoService.apply(config.mongodb)

  private val api: HttpRoutes[IO] = HttpRoutes
    .of[IO] {
      case GET -> Root / "report" =>
        Ok(generateReport().getHtml, Header("Content-Type", "text/html"))

      case GET -> Root / category =>
        category match {
          case InterestingTweet.typeName =>
            mongoService.tweets[InterestingTweet](category).flatMap(Ok(_))
          case AudioTweet.typeName =>
            mongoService.tweets[AudioTweet](category).flatMap(Ok(_))
          case VideoTweet.typeName =>
            mongoService.tweets[VideoTweet](category).flatMap(Ok(_))
          case ArticleTweet.typeName =>
            mongoService.tweets[ArticleTweet](category).flatMap(Ok(_))
          case VersionReleaseTweet.typeName =>
            mongoService.tweets[VersionReleaseTweet](category).flatMap(Ok(_))
          case ExcludedTweet.typeName =>
            mongoService.tweets[ExcludedTweet](category).flatMap(Ok(_))
          case _ => BadRequest()
        }

      case GET -> Root / category / "count" =>
        category match {
          case InterestingTweet.typeName | AudioTweet.typeName | VideoTweet.typeName | ArticleTweet.typeName |
              VersionReleaseTweet.typeName | ExcludedTweet.typeName =>
            mongoService.tweetsCount(category).flatMap(count => Ok(CountResult(count)))
          case _ => BadRequest()
        }

      case DELETE -> Root / category / tweetId =>
        mongoService.delete(category, tweetId).flatMap(_ => Ok("Deleted"))

      case PUT -> Root / "move" / tweetId :?
          SourceCategoryQueryParamMatcher(source) +& TargetCategoryQueryParamMatcher(target) =>
        mongoService.move(source, target, tweetId).flatMap(_ => Ok("Moved To Examinate collection"))
    }

  def run(args: List[String]): IO[ExitCode] =
    app.use(_ => IO.never).as(ExitCode.Success)

  val app: Resource[IO, Server[IO]] =
    for {
      blocker <- Blocker[IO]
      server <- BlazeServerBuilder[IO](global)
        .bindHttp(config.server.port, config.server.host)
        .withHttpApp(
          Router(
            "api" -> api,
            "" -> resourceService[IO](ResourceService.Config("/assets", blocker))
          ).orNotFound
        )
        .resource
    } yield server
}
