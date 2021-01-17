package com.kafka.experiments.tweetsui

import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import com.danielasfregola.twitter4s.TwitterRestClient
import com.kafka.experiments.tweetsui.api.{NewsletterApi, TweetApi}
import com.kafka.experiments.tweetsui.client.{GithubClient, MediumClient, MongoService, YoutubeClient}
import com.kafka.experiments.tweetsui.config.GlobalConfig
import com.kafka.experiments.tweetsui.newsletter.{FreeMarkerGenerator, NewsletterBuilder}
import com.kafka.experiments.tweetsui.client.sendgrid.SendGridClient
import com.kafka.experiments.tweetsui.score.ScoringService
import com.typesafe.scalalogging.StrictLogging
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.staticcontent.{ResourceService, resourceService}
import org.http4s.server.{Router, Server}
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContext.global

object Main extends IOApp with StrictLogging {
  import cats.implicits._

  private val config = ConfigSource.default.loadOrThrow[GlobalConfig]

  private val mongoService = MongoService(config.mongodb)
  private val fmGenerator = new FreeMarkerGenerator(config.freemarker)
  private val newsletterBuilder = new NewsletterBuilder(mongoService, fmGenerator)
  private val twitterRestClient = TwitterRestClient()

  val app: Resource[IO, Server[IO]] =
    for {
      blocker <- Blocker[IO]
      httpClient <- BlazeClientBuilder[IO](global).resource
      githubClient = GithubClient(config.github, httpClient)
      mediumClient = MediumClient(httpClient)
      youtubeClient = YoutubeClient(config.youtube, httpClient)
      scoringService = ScoringService(config.score, githubClient, mediumClient, twitterRestClient, youtubeClient)
      sendGridClient = SendGridClient(config.sendgrid, httpClient)
      newsletterApi = new NewsletterApi(newsletterBuilder, mongoService, scoringService, sendGridClient).api()
      tweetApi = new TweetApi(mongoService).api()
      server <- BlazeServerBuilder[IO](global)
        .bindHttp(config.server.port, config.server.host)
        .withHttpApp(
          Router(
            "api" -> (newsletterApi <+> tweetApi),
            "" -> resourceService[IO](ResourceService.Config("/assets", blocker))
          ).orNotFound
        )
        .resource
    } yield server

  def run(args: List[String]): IO[ExitCode] =
    app.use(_ => IO.never).as(ExitCode.Success)
}
