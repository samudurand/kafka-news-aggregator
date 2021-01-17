package com.kafka.experiments.tweetsui.score

import cats.effect.IO
import cats.implicits._
import com.kafka.experiments.tweetsui.client.MediumClient
import com.kafka.experiments.tweetsui.config.MediumScoringConfig
import com.kafka.experiments.tweetsui.newsletter.NewsletterTweet
import com.typesafe.scalalogging.StrictLogging

trait MediumScoreCalculator extends ScoreCalculator

object MediumScoreCalculator {

  def apply(config: MediumScoringConfig, client: MediumClient): MediumScoreCalculator =
    new DefaultMediumScoreCalculator(config, client)
}

class DefaultMediumScoreCalculator(config: MediumScoringConfig, client: MediumClient)
    extends MediumScoreCalculator
    with StrictLogging {

  val MediumDomain = "https://medium.com"

  override def calculate(tweets: Seq[NewsletterTweet]): IO[Map[String, Seq[Score]]] = {
    tweets
      .map {
        case tweet if hasMediumLink(tweet) => calculateScore(tweet).map(scores => tweet.id -> scores)
        case tweet => IO.pure(tweet.id -> List())
      }
      .toList
      .sequence
      .map(_.toMap)
  }

  private def hasMediumLink(tweet: NewsletterTweet): Boolean = {
    tweet.url.startsWith(MediumDomain)
  }

  private def calculateScore(tweet: NewsletterTweet): IO[Seq[Score]] = {
    client
      .retrieveClapCount(tweet.url)
      .map {
        case Some(claps) =>
          val clapsScore = calculateScaledScore("Medium Claps", config.claps, claps)
          List(clapsScore)
        case None => List()
      }
  }
}
