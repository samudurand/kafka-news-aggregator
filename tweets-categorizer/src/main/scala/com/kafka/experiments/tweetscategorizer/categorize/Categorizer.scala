package com.kafka.experiments.tweetscategorizer.categorize

import com.kafka.experiments.shared.{
  ArticleTweet,
  AudioTweet,
  CategorisedTweet,
  DroppedTweet,
  InterestingTweet,
  VersionReleaseTweet
}
import com.kafka.experiments.tweetscategorizer.TextUtils.{textContainAtLeastOneNumber, textLoweredCaseContainAnyOf}
import com.kafka.experiments.tweetscategorizer.tweetUtils.hasLink
import com.kafka.experiments.tweetscategorizer.Tweet
import com.kafka.experiments.tweetscategorizer.ignore.Keywords

object Categorizer {

  val reasonHasNoLink = "NO_CATEGORY_NOR_LINK"

  def categorize(tweet: Tweet): CategorisedTweet = {
    tweet match {
      case t if isAboutAnAudioPost(t) => AudioTweet(tweet.Id, tweet.Text, tweet.User.ScreenName)
      case t if isAboutANewVersion(t) => VersionReleaseTweet(tweet.Id, tweet.Text, tweet.User.ScreenName)
      case t if isAboutAnArticle(t)   => ArticleTweet(tweet.Id, tweet.Text, tweet.User.ScreenName)
      case t if hasLink(t)            => InterestingTweet(tweet.Id, tweet.Text, tweet.User.ScreenName)
      case _                          => DroppedTweet(tweet.Id, reasonHasNoLink, tweet.Text, tweet.User.ScreenName)
    }
  }

  private def isAboutAnAudioPost(tweet: Tweet): Boolean = {
    hasLink(tweet) && textLoweredCaseContainAnyOf(tweet.Text, Keywords.audioWords)
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
