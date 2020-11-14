package com.kafka.experiments.tweetscategorizer

import com.kafka.experiments.tweetscategorizer.config.GlobalConfig
import pureconfig._
import pureconfig.generic.auto._

object KnownSources {

  private val config = ConfigSource.default.load[GlobalConfig].getOrElse(throw new Exception("Unable to load config"))

  private val sourcesToBeIgnored = config.sources.ignored

  def hasSourceToBeIgnored(tweet: Tweet): Boolean = {
    sourcesToBeIgnored.exists(source => tweet.User.ScreenName.toLowerCase.equals(source.toLowerCase))
  }

}
