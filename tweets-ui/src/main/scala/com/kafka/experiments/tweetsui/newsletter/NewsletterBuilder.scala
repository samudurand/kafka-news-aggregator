package com.kafka.experiments.tweetsui.newsletter

import cats.effect.IO
import com.kafka.experiments.tweetsui.config.FreeMarkerConfig
import com.kafka.experiments.tweetsui.{Article, Audio, MongoService, Other, VersionRelease, Video}

import scala.jdk.CollectionConverters._
import com.linkedin.urls.detection.{UrlDetector, UrlDetectorOptions}

class NewsletterBuilder(mongoService: MongoService, freeMarkerConfig: FreeMarkerConfig) {

  val freeMarkerGenerator = new FreeMarkerGenerator(freeMarkerConfig)

  def buildNewsletter(): IO[String] = {
    mongoService
      .tweetsForNewsletter()
      .map(tweets =>
        tweets
          .groupBy(_.category)
          .map { case (category, tweetsByCategory) =>
            category match {
              case Article.name        => "listArticles" -> tweetsByCategory
              case Audio.name          => "listAudios" -> tweetsByCategory
              case VersionRelease.name => "listVersions" -> tweetsByCategory
              case Video.name          => "listVideos" -> tweetsByCategory
              case Other.name          => "listOthers" -> tweetsByCategory
              case _ =>
                throw new RuntimeException(s"Unable to retrieve for newsletter: unexpected category found [$category]")
            }
          }
      )
      .map(removeUrls)
      .map(_.view.mapValues(_.asJava).toMap)
      .map(data => freeMarkerGenerator.generateHtml(data))
  }

  private def removeUrls(data: Map[String, Seq[CompleteNewsletterTweet]]) = {
    data.view
      .mapValues(_.map { tweet =>
        val urls = new UrlDetector(tweet.text, UrlDetectorOptions.Default).detect()
        val cleanedText = urls.asScala.foldLeft(tweet.text)((text, url) => text.replace(url.getOriginalUrl, ""))
        tweet.copy(text = cleanedText)
      })
      .toMap
  }

}