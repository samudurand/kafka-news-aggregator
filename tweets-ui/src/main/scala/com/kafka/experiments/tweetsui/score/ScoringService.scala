package com.kafka.experiments.tweetsui.score

import cats.effect.{ContextShift, IO}
import cats.implicits._
import com.danielasfregola.twitter4s.TwitterRestClient
import com.kafka.experiments.tweetsui.client.{GithubClient, MediumClient, YoutubeClient}
import com.kafka.experiments.tweetsui.config.ScoringConfig
import com.kafka.experiments.tweetsui.newsletter.NewsletterTweet
import com.typesafe.scalalogging.StrictLogging

trait ScoringService {
  def calculateScores(tweets: Seq[NewsletterTweet]): IO[Seq[NewsletterTweet]]
}

object ScoringService {

  def apply(
      config: ScoringConfig,
      githubClient: GithubClient,
      mediumClient: MediumClient,
      twitterRestClient: TwitterRestClient,
      youtubeClient: YoutubeClient
  )(implicit
      context: ContextShift[IO]
  ): ScoringService =
    new DefaultScoringService(config, githubClient, mediumClient, twitterRestClient, youtubeClient)
}

class DefaultScoringService(
    config: ScoringConfig,
    githubClient: GithubClient,
    mediumClient: MediumClient,
    twitterRestClient: TwitterRestClient,
    youtubeClient: YoutubeClient
)(implicit
    context: ContextShift[IO]
) extends ScoringService
    with StrictLogging {

  private val githubScoreCalculator = GithubScoreCalculator(config.github, githubClient)
  private val mediumScoreCalculator = MediumScoreCalculator(config.medium, mediumClient)
  private val sourceScoreCalculator = SourceScoreCalculator(config.sources)
  private val twitterScoreCalculator = TwitterScoreCalculator(config.twitter, twitterRestClient)
  private val youtubeScoreCalculator = YoutubeScoreCalculator(config.youtube, youtubeClient)

  private val scoreCalculators =
    List(
      githubScoreCalculator,
      mediumScoreCalculator,
      sourceScoreCalculator,
      twitterScoreCalculator,
      youtubeScoreCalculator
    )

  def calculateScores(tweets: Seq[NewsletterTweet]): IO[Seq[NewsletterTweet]] = {
    scoreCalculators
      .map(_.calculate(tweets))
      .sequence
      .map(mergeScoresByTweet)
      .map(_.map { case (tweetId, scores) =>
        logger.info(s"Scores for tweet [$tweetId]: $scores")
        tweetId -> scores
      })
      .map(calculateAverageScores)
      .map(applyScoresToTweets(_, tweets))
  }

  private def applyScoresToTweets(scores: Map[String, Double], tweets: Seq[NewsletterTweet]) = {
    scores.map { case (tweetId, score) =>
      tweets.find(_.id == tweetId) match {
        case Some(tweet) => tweet.copy(score = Math.round(score))
        case None        => throw new RuntimeException("Tweet matching metadata not found! That should never happen.")
      }
    }.toSeq
  }

  def mergeScoresByTweet(scores: List[Map[String, Seq[Score]]]): Map[String, Seq[Score]] = {
    scores.foldLeft(Map[String, Seq[Score]]())((allScores, scores) =>
      (allScores.toSeq ++ scores.toSeq)
        .groupMap(_._1)(_._2)
        .view
        .mapValues(_.flatten)
        .toMap
    )
  }

  def calculateAverageScores(scores: Map[String, Seq[Score]]): Map[String, Double] = {
    scores.view.mapValues {
      case Nil => -1
      case scoresByTweets =>
        val scoreSum = scoresByTweets.map(score => score.value * score.factor).sum
        val factorSum = scoresByTweets.map(score => Math.abs(score.factor)).sum
        scoreSum.toDouble / factorSum.toDouble
    }.toMap
  }
}
