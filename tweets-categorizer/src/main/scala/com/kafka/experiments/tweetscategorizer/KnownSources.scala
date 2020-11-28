package com.kafka.experiments.tweetscategorizer

import com.kafka.experiments.tweetscategorizer.config.GlobalConfig
import pureconfig._
import pureconfig.generic.auto._

object KnownSources {

  private val config = ConfigSource.default.load[GlobalConfig].getOrElse(throw new Exception("Unable to load config"))

  private val sourcesToBeDropped = config.sources.dropped
  private val sourcesToBeAutoAccepted = config.sources.accepted

  def hasSourceToBeDropped(tweet: Tweet): Boolean = {
    sourcesToBeDropped.exists(source => tweet.User.ScreenName.toLowerCase.equals(source.toLowerCase))
  }

  def hasSourceToBeAutoAccepted(tweet: Tweet): Boolean = {
    sourcesToBeAutoAccepted.exists(source => tweet.User.ScreenName.toLowerCase.equals(source.toLowerCase))
  }

}
