package com.kafka.experiments.tweetscategorizer

import com.kafka.experiments.tweetscategorizer.config.GlobalConfig
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object Keywords {
  private val config = ConfigSource.default.loadOrThrow[GlobalConfig].keywords

  val adWords: Seq[String] = config.ad
  val articleWords: Seq[String] = config.article
  val articleDomains: Seq[String] = config.articledomains
  val audioWords: Seq[String] = config.audio
  val certification: Seq[String] = config.certification
  val discountWords: Seq[String] = config.discount
  val franzKafkaRelatedWords: Seq[String] = config.fkafka
  val jobOfferWords: Seq[String] = config.job
  val gameWords: Seq[String] = config.game
  val versionReleaseWords: Seq[String] = config.version
  val versionReleaseCombinedWords: Seq[String] = config.versioncombinations
  val videoWords: Seq[String] = config.video
  val videoDomains: Seq[String] = config.videodomains
  val unrelatedDomains: Seq[String] = config.unrelateddomains
  val unrelatedWords: Seq[String] = config.unrelated

}
