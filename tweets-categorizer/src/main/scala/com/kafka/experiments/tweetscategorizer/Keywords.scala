package com.kafka.experiments.tweetscategorizer

import com.kafka.experiments.tweetscategorizer.config.GlobalConfig
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object Keywords {
  private val config = ConfigSource.default.loadOrThrow[GlobalConfig].keywords

  val audioWords: Seq[String] = config.audio

  val articleWords: Seq[String] = config.article
  val articleDomains: Seq[String] = config.articledomains
  val discountWords: Seq[String] = config.discount
  val franzKafkaRelatedWords: Seq[String] = config.fkafka
  val jobOfferWords: Seq[String] = config.job
  val unrelatedWords: Seq[String] = config.unrelated
  val versionReleaseWords: Seq[String] = config.version
  val videoWords: Seq[String] = config.video
  val videoDomains: Seq[String] = config.videodomains

}
