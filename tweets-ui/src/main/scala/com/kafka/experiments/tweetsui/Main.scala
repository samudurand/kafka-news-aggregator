package com.kafka.experiments.tweetsui

import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import com.kafka.experiments.shared.{
  ArticleTweet,
  AudioTweet,
  ExcludedTweet,
  InterestingTweet,
  VersionReleaseTweet,
  VideoTweet
}
import com.kafka.experiments.tweetsui.Encoders._
import com.kafka.experiments.tweetsui.config.GlobalConfig
import com.kafka.experiments.tweetsui.report.ReportBuilder
import com.typesafe.scalalogging.StrictLogging
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.staticcontent.{resourceService, ResourceService}
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
  private val reportBuilder = new ReportBuilder(mongoService)

  private val api: HttpRoutes[IO] = HttpRoutes
    .of[IO] {
      case GET -> Root / "report" =>
        reportBuilder.buildReport().flatMap(Ok(_, Header("Content-Type", "text/html")))

      case GET -> Root / "tweets" / category              => getTweetsByCategory(category)
      case GET -> Root / "tweets" / category / "count"    => getTweetsCountByCategory(category)
      case DELETE -> Root / "tweets" / category           => deleteTweetsByCategory(category)
      case DELETE -> Root / "tweets" / category / tweetId => deleteTweet(category, tweetId)
      case PUT -> Root / "tweets" / "move" / tweetId :?
          SourceCategoryQueryParamMatcher(source) +& TargetCategoryQueryParamMatcher(target) =>
        mongoService.move(source, target, tweetId).flatMap(_ => Ok("Moved To Examinate collection"))
    }

  private def deleteTweet(categoryName: String, tweetId: String) = {
    TweetCategory.fromName(categoryName) match {
      case Some(category) => mongoService.delete(category, tweetId).flatMap(_ => Ok("Deleted"))
      case _              => BadRequest()
    }
  }

  private def deleteTweetsByCategory(categoryName: String) = {
    TweetCategory.fromName(categoryName) match {
      case Some(category) =>
        mongoService.deleteAll(category).flatMap(count => Ok(s"All tweets in category ${category} deleted"))
      case _ => BadRequest()
    }
  }

  private def getTweetsCountByCategory(categoryName: String) = {
    TweetCategory.fromName(categoryName) match {
      case Some(category) => mongoService.tweetsCount(category).flatMap(count => Ok(CountResult(count)))
      case _              => BadRequest()
    }
  }

  private def getTweetsByCategory(categoryName: String) = {
    TweetCategory.fromName(categoryName) match {
      case None => BadRequest()
      case Some(category) =>
        category match {
          case Interesting =>
            mongoService.tweets[InterestingTweet](category).flatMap(Ok(_))
          case Audio =>
            mongoService.tweets[AudioTweet](category).flatMap(Ok(_))
          case Video =>
            mongoService.tweets[VideoTweet](category).flatMap(Ok(_))
          case Article =>
            mongoService.tweets[ArticleTweet](category).flatMap(Ok(_))
          case VersionRelease =>
            mongoService.tweets[VersionReleaseTweet](category).flatMap(Ok(_))
          case Excluded =>
            mongoService.tweets[ExcludedTweet](category).flatMap(Ok(_))
        }
    }

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
