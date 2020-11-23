package com.kafka.experiments.tweetscategorizer.config

case class GlobalConfig(sources: SourceConfig, keywords: KeywordsConfig)

case class SourceConfig(ignored: List[String])

case class KeywordsConfig(
    article: Seq[String],
    audio: Seq[String],
    fkafka: Seq[String],
    job: Seq[String],
    unrelated: Seq[String],
    version: Seq[String]
)
