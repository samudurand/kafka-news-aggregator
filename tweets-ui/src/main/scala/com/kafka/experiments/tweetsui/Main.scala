package com.kafka.experiments.tweetsui

import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import com.kafka.experiments.shared.{
  ArticleTweet,
  AudioTweet,
  ExcludedTweet,
  OtherTweet,
  VersionReleaseTweet,
  VideoTweet
}
import com.kafka.experiments.tweetsui.Decoders._
import com.kafka.experiments.tweetsui.Encoders._
import com.kafka.experiments.tweetsui.config.GlobalConfig
import com.kafka.experiments.tweetsui.newsletter.{FreeMarkerGenerator, NewsletterBuilder}
import com.kafka.experiments.tweetsui.sendgrid.SendGridClient
import com.typesafe.scalalogging.StrictLogging
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.staticcontent.{resourceService, ResourceService}
import org.http4s.server.{Router, Server}
import org.http4s.{Header, HttpRoutes, Request, Response}
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContext.global

object CountResult {
  implicit val codec: Codec[CountResult] = deriveCodec
}
case class CountResult(count: Long)

object MoveTweetsToNewsletter {
  implicit val codec: Codec[MoveTweetsToNewsletter] = deriveCodec
}
case class MoveTweetsToNewsletter(tweetIds: Map[String, List[String]])

//object SourceCategoryQueryParamMatcher extends QueryParamDecoderMatcher[String]("source")
//object TargetCategoryQueryParamMatcher extends QueryParamDecoderMatcher[String]("target")

object Main extends IOApp with StrictLogging {
  import cats.implicits._

  private val config = ConfigSource.default.loadOrThrow[GlobalConfig]

  private val mongoService = MongoService(config.mongodb)
  private val fmGenerator = new FreeMarkerGenerator(config.freemarker)
  private val newsletterBuilder = new NewsletterBuilder(mongoService, fmGenerator)

  def api(sendGridClient: SendGridClient): HttpRoutes[IO] = HttpRoutes
    .of[IO] {
      case req @ PUT -> Root / "newsletter" / "prepare"      => prepareNewsletterData(req)
      case GET -> Root / "newsletter" / "included"           => loadCurrentlyIncludedInNewsletter()
      case GET -> Root / "newsletter" / "html"               => loadCurrentHtmlNewsletter()
      case POST -> Root / "newsletter" / "create"            => createNewsletterDraft(sendGridClient)
      case DELETE -> Root / "newsletter" / "reset"           => resetNewsletterData()
      case DELETE -> Root / "newsletter" / "tweet" / tweetId => deleteNewsletterTweet(tweetId)

      case GET -> Root / "tweets" / category / "count"    => getTweetsCountByCategory(category)
      case GET -> Root / "tweets" / category              => getTweetsByCategory(category)
      case DELETE -> Root / "tweets" / category           => deleteTweetsByCategory(category)
      case DELETE -> Root / "tweets" / category / tweetId => deleteTweet(category, tweetId)
//      case PUT -> Root / "tweets" / "move" / tweetId :?
//          SourceCategoryQueryParamMatcher(source) +& TargetCategoryQueryParamMatcher(target) =>
//        mongoService.move(source, target, tweetId).flatMap(_ => Ok("Moved To Examinate collection"))
    }

  private def loadCurrentlyIncludedInNewsletter(): IO[Response[IO]] = {
    mongoService.tweetsForNewsletter().flatMap(Ok(_))
  }

  private def loadCurrentHtmlNewsletter() = {
    newsletterBuilder.buildNewsletter().flatMap(Ok(_, Header("Content-Type", "text/html")))
  }

  private def prepareNewsletterData(req: Request[IO]) = {
    logger.info(req.as[MoveTweetsToNewsletter].toString())
    for {
      body <- req.as[MoveTweetsToNewsletter]
      counts <-
        body.tweetIds
          .map { case (category, tweetIds) =>
            mongoService.moveToNewsletter(TweetCategory.fromName(category).get, tweetIds)
          }
          .toList
          .sequence
      resp <- Ok(s"Moved ${counts.sum} tweets in the newsletter")
    } yield resp
  }

  def createNewsletterDraft(sendGridClient: SendGridClient): IO[Response[IO]] = {
    (for {
      html <- newsletterBuilder.buildNewsletter()
      _ <- sendGridClient.createSingleSend(html)
    } yield ()).flatMap(_ => Ok())
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

  private def deleteNewsletterTweet(tweetId: String) = {
    mongoService.deleteInNewsletter(tweetId).flatMap(Ok(_))
  }

  private def resetNewsletterData() = {
    mongoService.deleteAllInNewsletter().flatMap(Ok(_))
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
          case Other =>
            mongoService.tweets[OtherTweet](category).flatMap(Ok(_))
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
      httpClient <- BlazeClientBuilder[IO](global).resource
      sendGridClient = SendGridClient(config.sendgrid, httpClient)
      server <- BlazeServerBuilder[IO](global)
        .bindHttp(config.server.port, config.server.host)
        .withHttpApp(
          Router(
            "api" -> api(sendGridClient),
            "" -> resourceService[IO](ResourceService.Config("/assets", blocker))
          ).orNotFound
        )
        .resource
    } yield server
}
