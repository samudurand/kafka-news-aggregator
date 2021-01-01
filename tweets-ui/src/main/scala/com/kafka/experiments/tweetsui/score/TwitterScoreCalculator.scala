package com.kafka.experiments.tweetsui.score

import cats.effect.{ContextShift, IO}
import com.danielasfregola.twitter4s.TwitterRestClient
import com.kafka.experiments.tweetsui.config.TwitterConfig
import com.kafka.experiments.tweetsui.newsletter.NewsletterTweet
import cats.implicits._

trait TwitterScoreCalculator extends ScoreCalculator

object TwitterScoreCalculator {

  def apply(config: TwitterConfig, twitterRestClient: TwitterRestClient)(implicit
      context: ContextShift[IO]
  ): TwitterScoreCalculator =
    new DefaultTwitterScoreCalculator(config, twitterRestClient)
}

class DefaultTwitterScoreCalculator(config: TwitterConfig, twitterRestClient: TwitterRestClient)(implicit
    context: ContextShift[IO]
) extends TwitterScoreCalculator {
  private val maxTweetsByQuery = 100

  override def calculate(tweets: List[NewsletterTweet]): IO[Map[String, List[Score]]] = {
    for {
      metadata <- retrieveTwitterMetadata(tweets, maxTweetsByQuery)
      favScore = calculateFavoriteScores(metadata)
      follScore = calculateFollowerScores(metadata)
      retweetScore = calculateRetweetScores(metadata)
      scores = combineScores(favScore, follScore, retweetScore)
    } yield (scores)

    //    retrieveTwitterMetadata(tweets, maxTweetsByQuery)
    //      .map(_.map(metadata => {
    //        val score = calculateTwitterScore(metadata)
    //
    //        tweets.find(_.id == metadata.tweetId) match {
    //          case Some(tweet) => tweet.copy(score = Some(score))
    //          case None        => throw new RuntimeException("Tweet matching metadata not found! That should never happen.")
    //        }
    //      }))
  }

  private def calculateFavoriteScores(metadata: List[TweetsMetadata]): Map[String, Option[Score]] = {
    metadata.map(tweetMetadata => {
      val score = calculateCountScore(config.favourites.getScale, tweetMetadata.favoriteCount.toLong)
      tweetMetadata.tweetId -> score.map(Score(_, config.favourites.factor))
    }).toMap
  }

  private def calculateFollowerScores(metadata: List[TweetsMetadata]): Map[String, Option[Score]] = {
    metadata.map(tweetMetadata => {
      val score = tweetMetadata.userFollowersCount
        .flatMap(count => calculateCountScore(config.followers.getScale, count.toLong))
      tweetMetadata.tweetId -> score.map(Score(_, config.followers.factor))
    }).toMap
  }

  private def calculateRetweetScores(metadata: List[TweetsMetadata]): Map[String, Option[Score]] = {
    metadata.map(tweetMetadata => {
      val score = calculateCountScore(config.retweets.getScale, tweetMetadata.retweetCount)
      tweetMetadata.tweetId -> score.map(Score(_, config.retweets.factor))
    }).toMap
  }

  private def combineScores(favScore: Map[String, Option[Score]], follScore: Map[String, Option[Score]], retweetScore: Map[String, Option[Score]]) = {
    favScore.map { case (tweetId, score) => {
      val scores = List(score, follScore(tweetId), retweetScore(tweetId))
      tweetId -> scores.flatten
    }}
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

  private def calculateCountScore(scale: Map[Int, Int], count: Long) = {
    val countRange = determineScaleRange(scale, count)
    countRange.map(scale)
  }

  private def determineScaleRange(scale: Map[Int, Int], count: Long) = {
    scale.keys.toList.sorted.reverse.find(_ <= count)
  }
}

case class TweetsMetadata(
    tweetId: String,
    favoriteCount: Int,
    retweetCount: Long,
    userFollowersCount: Option[Int] = None
)
