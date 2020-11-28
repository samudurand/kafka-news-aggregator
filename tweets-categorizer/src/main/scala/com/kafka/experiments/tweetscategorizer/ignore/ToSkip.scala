package com.kafka.experiments.tweetscategorizer.ignore

import com.kafka.experiments.tweetscategorizer.Tweet
import com.typesafe.scalalogging.StrictLogging

object ToSkip extends StrictLogging {

  def shouldBeSkipped(tweet: Tweet): Boolean = {
    if (tweet.Retweet || isNotInEnglish(tweet)) {
      logger.debug(s"Tweet should be be skipped: $tweet")
      true
    } else {
      false
    }
  }

  private def isNotInEnglish(tweet: Tweet) = {
    tweet.Lang.isDefined && !tweet.Lang.getOrElse("").toLowerCase.equals("en")
  }

}
