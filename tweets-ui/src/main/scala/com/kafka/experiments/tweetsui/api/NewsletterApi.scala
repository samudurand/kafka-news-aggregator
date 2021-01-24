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
      case req @ PUT -> Root / "newsletter" / "prepare"      => prepareNewsletterData(req)
      case GET -> Root / "newsletter" / "included"           => loadCurrentlyIncludedInNewsletter()
      case GET -> Root / "newsletter" / "html"               => loadCurrentHtmlNewsletter()
      case POST -> Root / "newsletter" / "create"            => createNewsletterDraft(sendGridClient)
      case PUT -> Root / "newsletter" / "score"              => scoreNewsletterTweets(scoringService)
      case DELETE -> Root / "newsletter" / "reset"           => resetNewsletterData()
      case req @ PUT -> Root / "newsletter" / "tweet"        => updateTweet(req)
      case DELETE -> Root / "newsletter" / "tweet" / tweetId => deleteNewsletterTweet(tweetId)
    }

  def updateTweet(req: Request[IO]): IO[Response[IO]] = {
    for {
      body <- req.as[UpdateNewsletterTweet]
      _ <- {
        body match {
          case UpdateNewsletterTweet(tweetId, categoryOpt, favourite) =>
            val catUpdate = categoryOpt
              .flatMap { categoryName =>
                TweetCategory
                  .fromName(categoryName)
                  .map(category => mongoService.changeNewsletterCategory(tweetId, category))
              }
              .getOrElse(IO.unit)
            val favUpdate = favourite
              .map { fav =>
                mongoService.favouriteInNewsletter(tweetId, fav)
              }
              .getOrElse(IO.unit)
            List(catUpdate, favUpdate).sequence
        }
      }
      resp <- Ok(s"Updated")
    } yield resp
  }

  private def loadCurrentlyIncludedInNewsletter(): IO[Response[IO]] = {
    mongoService
      .tweetsForNewsletter()
      .map(_.sortBy(tweet => (tweet.favourite, tweet.score)).reverse)
      .flatMap(Ok(_))
  }

  private def loadCurrentHtmlNewsletter() = {
    newsletterBuilder.buildNewsletter().flatMap(Ok(_, Header("Content-Type", "text/html")))
  }

  private def prepareNewsletterData(req: Request[IO]) = {
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
      .flatMap(_.map(mongoService.updateNewsletterTweetScore).toList.sequence)
      .flatMap(_ => Ok("Scored"))
  }

  private def resetNewsletterData() = {
    mongoService.deleteAllInNewsletter().flatMap(Ok(_))
  }

  private def deleteNewsletterTweet(tweetId: String) = {
    mongoService.deleteInNewsletter(tweetId).flatMap(Ok(_))
  }
}
