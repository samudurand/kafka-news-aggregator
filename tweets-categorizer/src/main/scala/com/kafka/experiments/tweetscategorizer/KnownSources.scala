package com.kafka.experiments.tweetscategorizer

import com.kafka.experiments.tweetscategorizer.config.GlobalConfig
import pureconfig._
import pureconfig.generic.auto._

object KnownSources {

  private val config = ConfigSource.default.load[GlobalConfig].getOrElse(throw new Exception("Unable to load config"))

  private val sourcesToBeExcluded = config.sources.excluded
  private val sourcesToBeAutoAccepted = config.sources.accepted

  def hasSourceToBeExcluded(tweet: Tweet): Boolean = {
    sourcesToBeExcluded.exists(source => tweet.User.ScreenName.toLowerCase.equals(source.toLowerCase))
  }

  def hasSourceToBeAutoAccepted(tweet: Tweet): Boolean = {
    sourcesToBeAutoAccepted.exists(source => tweet.User.ScreenName.toLowerCase.equals(source.toLowerCase))
  }

}
