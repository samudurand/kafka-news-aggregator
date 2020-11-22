package com.kafka.experiments.tweetscategorizer.ignore

import com.kafka.experiments.tweetscategorizer.config.GlobalConfig
import pureconfig.ConfigSource

object Keywords {

  private val config = ConfigSource.default.loadOrThrow[GlobalConfig].keywords

  val audioWords: Seq[String] = config.audio
  val jobOfferWords: Seq[String] = config.job
  val articleWords: Seq[String] = config.article
  val unrelatedWords: Seq[String] = config.unrelated
  val versionReleaseWords: Seq[String] = config.version
}
