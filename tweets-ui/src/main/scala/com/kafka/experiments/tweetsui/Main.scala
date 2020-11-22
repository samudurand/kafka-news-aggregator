package com.kafka.experiments.tweetsui

import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import com.kafka.experiments.shared.{ArticleTweet, AudioTweet, DroppedTweet, InterestingTweet, VersionReleaseTweet}
import com.kafka.experiments.tweetsui.Encoders._
import com.kafka.experiments.tweetsui.config.GlobalConfig
import com.typesafe.scalalogging.StrictLogging
import io.circe.syntax._
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.staticcontent.{ResourceService, resourceService}
import org.http4s.server.{Router, Server}
import org.http4s.{HttpRoutes, MediaType}
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContext.global

object Main extends IOApp with StrictLogging {

  private val config = ConfigSource.default.loadOrThrow[GlobalConfig]

  private val mongoService = MongoService.apply(config.mongodb)

  private val api: HttpRoutes[IO] = HttpRoutes
    .of[IO] {
      case GET -> Root / category =>
        category match {
          case InterestingTweet.typeName =>
            mongoService.interestingTweets().flatMap(Ok(_))
          case AudioTweet.typeName =>
            mongoService.audioTweets().flatMap(Ok(_))
          case ArticleTweet.typeName =>
            mongoService.articleTweets().flatMap(Ok(_))
          case VersionReleaseTweet.typeName =>
            mongoService.versionTweets().flatMap(Ok(_))
          case DroppedTweet.typeName =>
            mongoService.droppedTweets().flatMap(Ok(_))
          case _ => BadRequest()
        }

      case GET -> Root / category / "count" =>
        category match {
          case InterestingTweet.typeName =>
            mongoService.interestingTweetsCount().flatMap(Ok(_))
          case DroppedTweet.typeName =>
            mongoService.droppedTweetsCount().flatMap(Ok(_))
          case _ => BadRequest()
        }

      case DELETE -> Root / category / LongVar(tweetId) =>
        mongoService.delete(category, tweetId).flatMap(_ => Ok("Deleted"))
    }

  def run(args: List[String]): IO[ExitCode] =
    app.use(_ => IO.never).as(ExitCode.Success)

  val app: Resource[IO, Server[IO]] =
    for {
      blocker <- Blocker[IO]
      server <- BlazeServerBuilder[IO](global)
        .bindHttp(config.server.port)
        .withHttpApp(
          Router(
            "api" -> api,
            "" -> resourceService[IO](ResourceService.Config("/assets", blocker))
          ).orNotFound
        )
        .resource
    } yield server

  private def retrieveInterestingTweets() = {
    val maybeTweets = for {
      intT <- mongoService.interestingTweets()
      audT <- mongoService.audioTweets()
      artT <- mongoService.articleTweets()
      verT <- mongoService.versionTweets()
    } yield {
      (
        intT.asJson.asArray ++
          audT.asJson.asArray ++
          artT.asJson.asArray ++
          verT.asJson.asArray
      ).flatten.asJson.noSpaces
    }

    maybeTweets.flatMap(Ok(_).map(_.withContentType(`Content-Type`(MediaType.application.json))))
  }
}
