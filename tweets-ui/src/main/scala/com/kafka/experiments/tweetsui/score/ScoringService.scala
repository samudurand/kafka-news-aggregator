package com.kafka.experiments.tweetsui.score

import cats.effect.{ContextShift, IO}
import cats.implicits._
import com.danielasfregola.twitter4s.TwitterRestClient
import com.kafka.experiments.tweetsui.config.ScoringConfig
import com.kafka.experiments.tweetsui.newsletter.NewsletterTweet
import com.typesafe.scalalogging.StrictLogging

trait ScoringService {
  def calculateScores(tweets: Seq[NewsletterTweet]): IO[Seq[NewsletterTweet]]
}

object ScoringService {

  def apply(config: ScoringConfig, twitterRestClient: TwitterRestClient)(implicit
      context: ContextShift[IO]
  ): ScoringService =
    new DefaultScoringService(config, twitterRestClient)
}

class DefaultScoringService(config: ScoringConfig, twitterRestClient: TwitterRestClient)(implicit
    context: ContextShift[IO]
) extends ScoringService with StrictLogging {

  private val twitterScoreCalculator = TwitterScoreCalculator(config.twitter, twitterRestClient)
  private val scoreCalculators = List(twitterScoreCalculator)

  def calculateScores(tweets: Seq[NewsletterTweet]): IO[Seq[NewsletterTweet]] = {
    scoreCalculators
      .map(_.calculate(tweets))
      .sequence
      .map(mergeScoresByTweet)
      .map(_.map{ case (tweetId, scores) =>
        logger.info(s"Scores for tweet [$tweetId]: $scores")
        tweetId -> scores
      })
      .map(calculateAverageScores)
      .map(applyScoresToTweets(_, tweets))
  }

  private def applyScoresToTweets(scores: Map[String, Option[Double]], tweets: Seq[NewsletterTweet]) = {
    scores.map { case (tweetId, score) =>
      tweets.find(_.id == tweetId) match {
        case Some(tweet) => tweet.copy(score = score.getOrElse(-1))
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

  def calculateAverageScores(scores: Map[String, Seq[Score]]): Map[String, Option[Double]] = {
    scores.view.mapValues {
      case Nil => None
      case scoresByTweets =>
        val scoreSum = scoresByTweets.map(score => score.value * score.factor).sum
        val factorSum = scoresByTweets.map(_.factor).sum
        Some(scoreSum.toDouble / factorSum.toDouble)
    }.toMap
  }
}