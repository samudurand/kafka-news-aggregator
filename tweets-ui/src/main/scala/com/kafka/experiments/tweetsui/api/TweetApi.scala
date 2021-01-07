package com.kafka.experiments.tweetsui.api

import cats.effect.IO
import com.kafka.experiments.shared._
import com.kafka.experiments.tweetsui.Encoders._
import com.kafka.experiments.tweetsui.client.MongoService
import com.kafka.experiments.tweetsui._
import com.typesafe.scalalogging.StrictLogging
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

class TweetApi(mongoService: MongoService) extends StrictLogging {

  def api(): HttpRoutes[IO] = HttpRoutes
    .of[IO] {
      case GET -> Root / "tweets" / category / "count"    => getTweetsCountByCategory(category)
      case GET -> Root / "tweets" / category              => getTweetsByCategory(category)
      case DELETE -> Root / "tweets" / category           => deleteTweetsByCategory(category)
      case DELETE -> Root / "tweets" / category / tweetId => deleteTweet(category, tweetId)
    }

  private def deleteTweet(categoryName: String, tweetId: String) = {
    TweetCategory.fromName(categoryName) match {
      case Some(category) => mongoService.delete(category, tweetId).flatMap(_ => Ok("Deleted"))
      case _              => BadRequest()
    }
  }

  private def deleteTweetsByCategory(categoryName: String) = {
    TweetCategory.fromName(categoryName) match {
      case Some(category) =>
        mongoService.deleteAll(category).flatMap(count => Ok(s"All tweets in category ${category} deleted"))
      case _ => BadRequest()
    }
  }

  private def getTweetsCountByCategory(categoryName: String) = {
    TweetCategory.fromName(categoryName) match {
      case Some(category) => mongoService.tweetsCount(category).flatMap(count => Ok(CountResult(count)))
      case _              => BadRequest()
    }
  }

  private def getTweetsByCategory(categoryName: String) = {
    TweetCategory.fromName(categoryName) match {
      case None => BadRequest()
      case Some(category) =>
        category match {
          case Other =>
            mongoService.tweets[OtherTweet](category).flatMap(Ok(_))
          case Audio =>
            mongoService.tweets[AudioTweet](category).flatMap(Ok(_))
          case Video =>
            mongoService.tweets[VideoTweet](category).flatMap(Ok(_))
          case Article =>
            mongoService.tweets[ArticleTweet](category).flatMap(Ok(_))
          case Tool =>
            mongoService.tweets[ToolTweet](category).flatMap(Ok(_))
          case VersionRelease =>
            mongoService.tweets[VersionReleaseTweet](category).flatMap(Ok(_))
          case Excluded =>
            mongoService.tweets[ExcludedTweet](category).flatMap(Ok(_))
        }
    }
  }
}
