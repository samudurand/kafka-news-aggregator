package com.kafka.experiments.tweetsui

import cats.effect.{ContextShift, IO}
import com.danielasfregola.twitter4s.TwitterRestClient
import com.kafka.experiments.tweetsui.newsletter.NewsletterTweet
import cats.implicits._
import com.kafka.experiments.tweetsui.config.ScoringConfig

case class TweetsMetadata(
    tweetId: String,
    favoriteCount: Int,
    retweetCount: Long,
    userFollowersCount: Option[Int] = None
)

trait ScoringService {
  def calculateScores(tweets: Seq[NewsletterTweet]): IO[Seq[NewsletterTweet]]
}

object ScoringService {

  def apply(config: ScoringConfig, twitterRestClient: TwitterRestClient)(implicit
      context: ContextShift[IO]
  ): ScoringService =
    new DefaultScoringService(config, twitterRestClient)
}

class DefaultScoringService(config: ScoringConfig, twitterRestClient: TwitterRestClient)(
    implicit context: ContextShift[IO]
) extends ScoringService {

  def calculateScores(tweets: Seq[NewsletterTweet]): IO[Seq[NewsletterTweet]] = {
    val maxByQuery = 100

    for {
      tweetMetadata <- retrieveTwitterMetadata(tweets, maxByQuery)
      scoredTweets = calculateScore(tweetMetadata, tweets)
    } yield (scoredTweets)
  }

  private def retrieveTwitterMetadata(tweets: Seq[NewsletterTweet], maxByQuery: Int): IO[List[TweetsMetadata]] = {
    tweets
      .map(_.id.toLong)
      .grouped(maxByQuery)
      .map(twitterRestClient.tweetLookup(_: _*))
      .map(res => IO.fromFuture(IO(res)))
      .toList
      .sequence
      .map(_.flatMap(_.data))
      .map(
        _.map(metadata =>
          metadata.user match {
            case Some(user) =>
              TweetsMetadata(
                metadata.id_str,
                metadata.favorite_count,
                metadata.retweet_count,
                Some(user.followers_count)
              )
            case None => TweetsMetadata(metadata.id_str, metadata.favorite_count, metadata.retweet_count)
          }
        )
      )
  }

  def calculateScore(tweetsMetadata: List[TweetsMetadata], tweets: Seq[NewsletterTweet]): List[NewsletterTweet] = {
    tweetsMetadata.map(metadata => {
      val score = calculateTwitterScore(metadata)

      tweets.find(_.id == metadata.tweetId) match {
        case Some(tweet) => tweet.copy(score = Some(score))
        case None        => throw new RuntimeException("Tweet matching metadata not found! That should never happen.")
      }
    })
  }

  private def calculateTwitterScore(metadata: TweetsMetadata) = {
    val favCountScore: Int = calculateCountScore(config.scaleFavourites, metadata.favoriteCount.toLong).getOrElse(0)
    val follCountScore: Int =
      metadata.userFollowersCount
        .flatMap(count => calculateCountScore(config.scaleFollowers, count.toLong))
        .getOrElse(0)
    val retweetCountScore: Int = calculateCountScore(config.scaleRetweets, metadata.retweetCount).getOrElse(0)
    List(favCountScore, follCountScore, retweetCountScore).sum
  }

  private def calculateCountScore(scale: Map[Int, Int], count: Long) = {
    val countRange = determineScaleRange(scale, count)
    countRange.map(scale)
  }

  private def determineScaleRange(scale: Map[Int, Int], count: Long) = {
    scale.keys.toList.sorted.reverse.find(_ <= count)
  }
}
