package com.kafka.experiments.tweetscategorizer.utils

import com.kafka.experiments.tweetscategorizer.{Tweet, URLEntity}

object TweetUtils {

  val twitterDomain = "https://twitter.com"

  def hasValidLink(tweet: Tweet): Boolean = {
    !tweet.URLEntities.forall(_.ExpandedURL.startsWith(twitterDomain))
  }

  def firstValidLink(tweet: Tweet): Option[URLEntity] = {
    tweet.URLEntities.find(!_.ExpandedURL.startsWith(twitterDomain))
  }

}
