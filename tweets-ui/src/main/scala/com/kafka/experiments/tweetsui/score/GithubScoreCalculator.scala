package com.kafka.experiments.tweetsui.score

import cats.effect.IO
import cats.implicits._
import com.kafka.experiments.tweetsui.client.GithubClient
import com.kafka.experiments.tweetsui.config.GithubScoringConfig
import com.kafka.experiments.tweetsui.newsletter.NewsletterTweet
import com.typesafe.scalalogging.StrictLogging

trait GithubScoreCalculator extends ScoreCalculator

object GithubScoreCalculator {

  def apply(config: GithubScoringConfig, client: GithubClient): GithubScoreCalculator =
    new DefaultGithubScoreCalculator(config, client)
}

class DefaultGithubScoreCalculator(config: GithubScoringConfig, client: GithubClient)
    extends GithubScoreCalculator
    with StrictLogging {

  val githubDomain = "https://github.com"

  override def calculate(tweets: Seq[NewsletterTweet]): IO[Map[String, Seq[Score]]] = {
    tweets
      .map {
        case tweet if hasGithubLink(tweet) => calculateScore(tweet).map(scores => tweet.id -> scores)
        case tweet => IO.pure(tweet.id -> List())
      }
      .toList
      .sequence
      .map(_.toMap)
  }

  private def hasGithubLink(tweet: NewsletterTweet): Boolean = {
    tweet.url.startsWith(githubDomain)
  }

  private def calculateScore(tweet: NewsletterTweet): IO[Seq[Score]] = {
    client
      .retrieveRepoMetadata(tweet.url)
      .map {
        case Some(metadata) =>
          val starsScore = calculateScaledScore("Github Stars", config.stars, metadata.stargazers_count)
          val watchersScore = calculateScaledScore("Github Watchers", config.watchers, metadata.watchers_count)
          List(starsScore, watchersScore)
        case None => List()
      }
  }
}
