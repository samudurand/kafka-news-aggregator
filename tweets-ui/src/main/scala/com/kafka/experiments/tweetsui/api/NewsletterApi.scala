package com.kafka.experiments.tweetsui.api

import cats.effect.IO
import cats.implicits._
import com.kafka.experiments.tweetsui.Decoders._
import com.kafka.experiments.tweetsui.Encoders._
import com.kafka.experiments.tweetsui.client.MongoService
import com.kafka.experiments.tweetsui.client.sendgrid.SendGridClient
import com.kafka.experiments.tweetsui.newsletter.NewsletterBuilder
import com.kafka.experiments.tweetsui.TweetCategory
import com.kafka.experiments.tweetsui.score.ScoringService
import com.typesafe.scalalogging.StrictLogging
import org.http4s.dsl.io._
import org.http4s.{Header, HttpRoutes, Request, Response}

class NewsletterApi(
    newsletterBuilder: NewsletterBuilder,
    mongoService: MongoService,
    scoringService: ScoringService,
    sendGridClient: SendGridClient
) extends StrictLogging {

  def api(): HttpRoutes[IO] = HttpRoutes
    .of[IO] {
      case req @ PUT -> Root / "newsletter" / "prepare"              => prepareNewsletterData(req)
      case GET -> Root / "newsletter" / "included"                   => loadCurrentlyIncludedInNewsletter()
      case GET -> Root / "newsletter" / "html"                       => loadCurrentHtmlNewsletter()
      case POST -> Root / "newsletter" / "create"                    => createNewsletterDraft(sendGridClient)
      case PUT -> Root / "newsletter" / "score"                      => scoreNewsletterTweets(scoringService)
      case DELETE -> Root / "newsletter" / "reset"                   => resetNewsletterData()
      case PUT -> Root / "newsletter" / "tweet" / tweetId / category => setTweetCategory(tweetId, category)
      case DELETE -> Root / "newsletter" / "tweet" / tweetId         => deleteNewsletterTweet(tweetId)
    }

  private def loadCurrentlyIncludedInNewsletter(): IO[Response[IO]] = {
    mongoService
      .tweetsForNewsletter()
      .map(_.sortBy(_.score).reverse)
      .flatMap(Ok(_))
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

  private def scoreNewsletterTweets(scoringService: ScoringService): IO[Response[IO]] = {
    mongoService
      .tweetsForNewsletter()
      .flatMap(scoringService.calculateScores)
      .flatMap(_.map(mongoService.updateNewsletterTweet).toList.sequence)
      .flatMap(_ => Ok("Scored"))
  }

  private def resetNewsletterData() = {
    mongoService.deleteAllInNewsletter().flatMap(Ok(_))
  }

  private def deleteNewsletterTweet(tweetId: String) = {
    mongoService.deleteInNewsletter(tweetId).flatMap(Ok(_))
  }

  private def setTweetCategory(tweetId: String, categoryName: String) = {
    TweetCategory.fromName(categoryName) match {
      case Some(category) => mongoService.changeNewsletterCategory(tweetId, category).flatMap(Ok(_))
      case None => BadRequest()
    }
  }
}
