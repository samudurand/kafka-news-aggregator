package com.kafka.experiments.shared

import com.kafka.experiments.shared.UrlManipulator.expandUrlOnce

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
  def noParamsUrl(url: String): String = {
    url.replaceAll("\\?.*$", "")
  }

  def expandUrlAndExtractBase(url: String): String = {
    noParamsUrl(expandUrlOnce(url))
  }

}
