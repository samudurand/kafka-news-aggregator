package com.kafka.experiments.tweetsui.score

import cats.effect.{ContextShift, IO}
import com.danielasfregola.twitter4s.TwitterRestClient
import com.kafka.experiments.tweetsui.config.TwitterScoringConfig
import com.kafka.experiments.tweetsui.newsletter.NewsletterTweet
import cats.implicits._

trait TwitterScoreCalculator extends ScoreCalculator

object TwitterScoreCalculator {

  def apply(config: TwitterScoringConfig, twitterRestClient: TwitterRestClient)(implicit
      context: ContextShift[IO]
  ): TwitterScoreCalculator =
    new DefaultTwitterScoreCalculator(config, twitterRestClient)
}

class DefaultTwitterScoreCalculator(config: TwitterScoringConfig, twitterRestClient: TwitterRestClient)(implicit
    context: ContextShift[IO]
) extends TwitterScoreCalculator {
  private val maxTweetsByQuery = 100

  override def calculate(tweets: Seq[NewsletterTweet]): IO[Map[String, Seq[Score]]] = {
    for {
      metadata <- retrieveTwitterMetadata(tweets, maxTweetsByQuery)
      favScore = calculateFavoriteScores(metadata)
      follScore = calculateFollowerScores(metadata)
      retweetScore = calculateRetweetScores(metadata)
      scores = combineScores(favScore, follScore, retweetScore)
    } yield scores
  }

  private def calculateFavoriteScores(metadata: List[TweetsMetadata]): Map[String, Score] = {
    metadata
      .map(tweetMetadata => {
        val score = calculateCountScore(config.favourites.getScale, tweetMetadata.favoriteCount.toLong)
        tweetMetadata.tweetId -> Score("Twitter Favourites", score, config.favourites.factor)
      })
      .toMap
  }

  private def calculateFollowerScores(metadata: List[TweetsMetadata]): Map[String, Option[Score]] = {
    metadata
      .map(tweetMetadata => {
        val score = tweetMetadata.userFollowersCount
          .map(count => calculateCountScore(config.followers.getScale, count.toLong))
        tweetMetadata.tweetId -> score.map(Score("Twitter Followers", _, config.followers.factor))
      })
      .toMap
  }

  private def calculateRetweetScores(metadata: List[TweetsMetadata]): Map[String, Score] = {
    metadata
      .map(tweetMetadata => {
        val score = calculateCountScore(config.retweets.getScale, tweetMetadata.retweetCount)
        tweetMetadata.tweetId -> Score("Twitter Retweets", score, config.retweets.factor)
      })
      .toMap
  }

  private def combineScores(
      favScore: Map[String, Score],
      follScore: Map[String, Option[Score]],
      retweetScore: Map[String, Score]
  ): Map[String, List[Score]] = {
    favScore.map { case (tweetId, score) =>
      val scores = List(Some(score), follScore.get(tweetId).flatten, retweetScore.get(tweetId))
      tweetId -> scores.flatten
    }
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
}

case class TweetsMetadata(
    tweetId: String,
    favoriteCount: Int,
    retweetCount: Long,
    userFollowersCount: Option[Int] = None
)
