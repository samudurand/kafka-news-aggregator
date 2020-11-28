package com.kafka.experiments.tweetscategorizer.config

case class GlobalConfig(sources: SourceConfig, keywords: KeywordsConfig)

case class SourceConfig(accepted: List[String], excluded: List[String])

case class KeywordsConfig(
    article: Seq[String],
    articledomains: Seq[String],
    audio: Seq[String],
    discount: Seq[String],
    fkafka: Seq[String],
    job: Seq[String],
    unrelated: Seq[String],
    version: Seq[String],
    video: Seq[String],
    videodomains: Seq[String]
)
