package com.kafka.experiments.tweetscategorizer.categorize

import com.kafka.experiments.shared._
import com.kafka.experiments.tweetscategorizer.TextUtils.{textContainAtLeastOneNumber, textLoweredCaseContainAnyOf}
import com.kafka.experiments.tweetscategorizer.{Keywords, Tweet}
import com.kafka.experiments.tweetscategorizer.tweetUtils.{firstValidLink, hasValidLink}

object Categorizer {

  val reasonHasNoLink = "NO_CATEGORY_NOR_LINK"

  def categorize(tweet: Tweet): CategorisedTweet = {
    firstValidLink(tweet) match {
      case None =>
        ExcludedTweet(tweet.Id.toString, reasonHasNoLink, tweet.Text, tweet.User.ScreenName, tweet.CreatedAt.toString)
      case Some(urlEntity) =>
        val validLink = urlEntity.ExpandedURL
        tweet match {
          case t if isAboutANewVersion(t) =>
            VersionReleaseTweet(
              tweet.Id.toString,
              tweet.Text,
              validLink,
              tweet.User.ScreenName,
              tweet.CreatedAt.toString
            )
          case t if isAboutAnAudioPost(t) =>
            AudioTweet(tweet.Id.toString, tweet.Text, validLink, tweet.User.ScreenName, tweet.CreatedAt.toString)
          case t if isAboutAVideoPost(t) =>
            VideoTweet(tweet.Id.toString, tweet.Text, validLink, tweet.User.ScreenName, tweet.CreatedAt.toString)
          case t if isAboutAnArticle(t) =>
            ArticleTweet(tweet.Id.toString, tweet.Text, validLink, tweet.User.ScreenName, tweet.CreatedAt.toString)
          case _ =>
            OtherTweet(tweet.Id.toString, tweet.Text, validLink, tweet.User.ScreenName, tweet.CreatedAt.toString)
        }
    }
  }

  private def isAboutAnAudioPost(tweet: Tweet): Boolean = {
    textLoweredCaseContainAnyOf(tweet.Text, Keywords.audioWords)
  }

  private def isAboutAVideoPost(tweet: Tweet): Boolean = {
    textLoweredCaseContainAnyOf(tweet.Text, Keywords.videoWords) ||
    Keywords.videoDomains.exists(domain => tweet.URLEntities.exists(_.ExpandedURL.contains(domain)))
  }

  private def isAboutANewVersion(tweet: Tweet): Boolean = {
    textLoweredCaseContainAnyOf(tweet.Text, Keywords.versionReleaseWords) &&
    textContainAtLeastOneNumber(tweet.Text)
  }

  private def isAboutAnArticle(tweet: Tweet): Boolean = {
    textLoweredCaseContainAnyOf(tweet.Text, Keywords.articleWords) ||
    Keywords.articleDomains.exists(domain => tweet.URLEntities.exists(_.ExpandedURL.contains(domain)))
  }

}
