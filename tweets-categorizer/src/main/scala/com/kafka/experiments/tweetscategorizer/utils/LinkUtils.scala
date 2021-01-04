package com.kafka.experiments.tweetscategorizer.utils

import com.kafka.experiments.shared.UrlManipulator.expandUrlOnce
import com.kafka.experiments.tweetscategorizer.URLEntity

object LinkUtils {

  private val twitterDomain = "https://twitter.com"

  def containsValidLink(links: List[URLEntity]): Boolean = {
    !links.forall(_.ExpandedURL.startsWith(twitterDomain))
  }

  def firstValidLink(links: List[URLEntity]): Option[URLEntity] = {
    links.find(!_.ExpandedURL.startsWith(twitterDomain))
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
