package com.kafka.experiments.tweetscategorizer.categorize

import com.kafka.experiments.shared._
import com.kafka.experiments.tweetscategorizer.TextUtils.{textContainAtLeastOneNumber, textLoweredCaseContainAnyOf}
import com.kafka.experiments.tweetscategorizer.{Keywords, Tweet}
import com.kafka.experiments.tweetscategorizer.tweetUtils.hasLink

object Categorizer {

  val reasonHasNoLink = "NO_CATEGORY_NOR_LINK"

  def categorize(tweet: Tweet): CategorisedTweet = {
    tweet match {
      case t if isAboutAnAudioPost(t) =>
        AudioTweet(tweet.Id.toString, tweet.Text, tweet.User.ScreenName, tweet.CreatedAt.toString)
      case t if isAboutAVideoPost(t) =>
        VideoTweet(tweet.Id.toString, tweet.Text, tweet.User.ScreenName, tweet.CreatedAt.toString)
      case t if isAboutANewVersion(t) =>
        VersionReleaseTweet(tweet.Id.toString, tweet.Text, tweet.User.ScreenName, tweet.CreatedAt.toString)
      case t if isAboutAnArticle(t) =>
        ArticleTweet(tweet.Id.toString, tweet.Text, tweet.User.ScreenName, tweet.CreatedAt.toString)
      case t if hasLink(t) =>
        InterestingTweet(tweet.Id.toString, tweet.Text, tweet.User.ScreenName, tweet.CreatedAt.toString)
      case _ =>
        DroppedTweet(tweet.Id.toString, reasonHasNoLink, tweet.Text, tweet.User.ScreenName, tweet.CreatedAt.toString)
    }
  }

  private def isAboutAnAudioPost(tweet: Tweet): Boolean = {
    hasLink(tweet) && textLoweredCaseContainAnyOf(tweet.Text, Keywords.audioWords)
  }

  private def isAboutAVideoPost(tweet: Tweet): Boolean = {
    hasLink(tweet) && (
      textLoweredCaseContainAnyOf(tweet.Text, Keywords.videoWords) ||
        Keywords.videoDomains.exists(domain => tweet.URLEntities.exists(_.ExpandedURL.contains(domain)))
    )
  }

  private def isAboutANewVersion(tweet: Tweet): Boolean = {
    hasLink(tweet) &&
    textLoweredCaseContainAnyOf(tweet.Text, Keywords.versionReleaseWords) &&
    textContainAtLeastOneNumber(tweet.Text)
  }

  private def isAboutAnArticle(tweet: Tweet): Boolean = {
    hasLink(tweet) && textLoweredCaseContainAnyOf(tweet.Text, Keywords.articleWords)
  }

}
