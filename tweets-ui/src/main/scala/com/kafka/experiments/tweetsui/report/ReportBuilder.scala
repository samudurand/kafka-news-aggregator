package com.kafka.experiments.tweetsui.report

import cats.effect.IO
import com.kafka.experiments.tweetsui.{Article, Audio, Interesting, MongoService, TweetCategory, VersionRelease, Video}

import scala.jdk.CollectionConverters._
import com.kafka.experiments.shared.{ArticleTweet, AudioTweet, InterestingTweet, VersionReleaseTweet, VideoTweet}

class ReportBuilder(mongoService: MongoService) {

  val freeMarkerGenerator = new FreeMarkerGenerator()

  def buildReport(): IO[String] = {
    (for {
      articleTweets <- mongoService.tweets[ArticleTweet](Article)
      audioTweets <- mongoService.tweets[AudioTweet](Audio)
      interestingTweets <- mongoService.tweets[InterestingTweet](Interesting)
      versionTweets <- mongoService.tweets[VersionReleaseTweet](VersionRelease)
      videoTweets <- mongoService.tweets[VideoTweet](Video)
      templateData = Map(
        "listArticles" -> articleTweets.map(ReportTweet(_)).asJava,
        "listAudios" -> audioTweets.map(ReportTweet(_)).asJava,
        "listOthers" -> interestingTweets.map(ReportTweet(_)).asJava,
        "listVersions" -> versionTweets.map(ReportTweet(_)).asJava,
        "listVideos" -> videoTweets.map(ReportTweet(_)).asJava
      )
    } yield (templateData))
      .map(data => freeMarkerGenerator.generateHtml(data))
  }

}
