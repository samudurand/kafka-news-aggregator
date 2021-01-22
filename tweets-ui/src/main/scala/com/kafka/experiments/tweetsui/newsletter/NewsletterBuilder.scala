package com.kafka.experiments.tweetsui.newsletter

import cats.effect.IO
import com.kafka.experiments.shared.UrlManipulator
import com.kafka.experiments.shared.UrlManipulator.removeUrls
import com.kafka.experiments.tweetsui._
import com.kafka.experiments.tweetsui.client.MongoService
import com.kafka.experiments.tweetsui.config.NewsletterConfig
import com.linkedin.urls.detection.{UrlDetector, UrlDetectorOptions}

import scala.jdk.CollectionConverters._

class NewsletterBuilder(mongoService: MongoService, fmGenerator: FreeMarkerGenerator, config: NewsletterConfig) {

  def buildNewsletter(): IO[String] = {
    mongoService
      .tweetsForNewsletter()
      .map(tweets =>
        tweets
          .sortBy(_.score)(Ordering[Long].reverse)
          .groupBy(_.category)
          .map { case (category, allTweetsByCategory) =>
            val tweetsByCategory = takeUpToMax(allTweetsByCategory)
            category match {
              case Article.name        => "listArticles" -> tweetsByCategory
              case Audio.name          => "listAudios" -> tweetsByCategory
              case Tool.name           => "listTools" -> tweetsByCategory
              case VersionRelease.name => "listVersions" -> tweetsByCategory
              case Video.name          => "listVideos" -> tweetsByCategory
              case Other.name          => "listOthers" -> tweetsByCategory
              case _ =>
                throw new RuntimeException(s"Unable to retrieve for newsletter: unexpected category found [$category]")
            }
          }
      )
      .map(removeUrlsInTweets)
      .map(_.view.mapValues(_.asJava).toMap)
      .map(data => fmGenerator.generateHtml(data))
  }

  private def takeUpToMax(tweets: Seq[NewsletterTweet]) = {
    if (tweets.size > config.maxByCategory) {
      tweets.take(config.maxByCategory)
    } else {
      tweets
    }
  }

  private def removeUrlsInTweets(data: Map[String, Seq[NewsletterTweet]]) = {
    data.view
      .mapValues(_.map { tweet =>
        tweet.copy(text = removeUrls(tweet.text))
      })
      .toMap
  }
}
