package com.kafka.experiments.tweetsui.report

import cats.effect.IO
import com.kafka.experiments.tweetsui.{Article, Audio, Interesting, MongoService, TweetCategory, VersionRelease, Video}

import scala.jdk.CollectionConverters._
import com.kafka.experiments.shared.{ArticleTweet, AudioTweet, OtherTweet, VersionReleaseTweet, VideoTweet}
import com.linkedin.urls.detection.{UrlDetector, UrlDetectorOptions}

class ReportBuilder(mongoService: MongoService) {

  val freeMarkerGenerator = new FreeMarkerGenerator()

  def buildReport(): IO[String] = {
    (for {
      articleTweets <- mongoService.tweets[ArticleTweet](Article)
      audioTweets <- mongoService.tweets[AudioTweet](Audio)
      interestingTweets <- mongoService.tweets[OtherTweet](Interesting)
      versionTweets <- mongoService.tweets[VersionReleaseTweet](VersionRelease)
      videoTweets <- mongoService.tweets[VideoTweet](Video)
      templateData = Map(
        "listArticles" -> articleTweets.map(ReportTweet(_)),
        "listAudios" -> audioTweets.map(ReportTweet(_)),
        "listVideos" -> videoTweets.map(ReportTweet(_)),
        "listVersions" -> versionTweets.map(ReportTweet(_)),
        "listOthers" -> interestingTweets.map(ReportTweet(_))
      )
    } yield templateData)
      .map { data => removeUrls(data) }
      .map { data => data.view.mapValues(_.asJava).toMap }
      .map(data => freeMarkerGenerator.generateHtml(data))
  }

  private def removeUrls(data: Map[String, Seq[ReportTweet]]) = {
    data.view
      .mapValues(_.map { tweet =>
        val urls = new UrlDetector(tweet.text, UrlDetectorOptions.Default).detect()
        val cleanedText = urls.asScala.foldLeft(tweet.text)((text, url) => text.replace(url.getOriginalUrl, ""))
        tweet.copy(text = cleanedText)
      })
      .toMap
  }

}
