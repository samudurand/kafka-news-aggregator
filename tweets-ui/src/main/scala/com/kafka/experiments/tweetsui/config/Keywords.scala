package com.kafka.experiments.tweetsui.config

import pureconfig.ConfigSource
import pureconfig.generic.auto._

object Keywords {
  private val config = ConfigSource.default.loadOrThrow[GlobalConfig].keywords

  val relatedWords: Seq[String] = config.related
}
