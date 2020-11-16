package com.kafka.experiments.tweetscategorizer

object tweetUtils {

  def hasLink(tweet: Tweet): Boolean = {
    tweet.URLEntities.nonEmpty
  }

}
