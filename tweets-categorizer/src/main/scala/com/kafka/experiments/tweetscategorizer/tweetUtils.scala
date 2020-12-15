package com.kafka.experiments.tweetscategorizer

object tweetUtils {

  val twitterDomain = "https://twitter.com"

  def hasValidLink(tweet: Tweet): Boolean = {
    !tweet.URLEntities.forall(_.ExpandedURL.startsWith(twitterDomain))
  }

  def firstValidLink(tweet: Tweet): Option[URLEntity] = {
    tweet.URLEntities.find(!_.ExpandedURL.startsWith(twitterDomain))
  }

}
