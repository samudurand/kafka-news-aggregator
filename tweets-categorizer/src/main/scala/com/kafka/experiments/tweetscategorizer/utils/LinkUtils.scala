package com.kafka.experiments.tweetscategorizer.utils

import com.kafka.experiments.shared.UrlManipulator
import com.kafka.experiments.shared.UrlManipulator.expandUrlOnce
import com.kafka.experiments.tweetscategorizer.{Tweet, URLEntity}

object LinkUtils {

  val twitterDomain = "https://twitter.com"

  def hasValidLink(tweet: Tweet): Boolean = {
    !tweet.URLEntities.forall(_.ExpandedURL.startsWith(twitterDomain))
  }

  def firstValidLink(tweet: Tweet): Option[URLEntity] = {
    tweet.URLEntities.find(!_.ExpandedURL.startsWith(twitterDomain))
  }

  /**
   * Extract the base URL (without parameters)
   */
  def extractBaseUrl(url: String): String = {
    url.replaceAll("\\?.*$", "")
  }

  def expandUrlAndExtractBase(url: String): String = {
    extractBaseUrl(expandUrlOnce(url))
  }

}
