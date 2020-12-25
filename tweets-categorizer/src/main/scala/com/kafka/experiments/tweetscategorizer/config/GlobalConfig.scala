package com.kafka.experiments.tweetscategorizer.config

case class GlobalConfig(kafka: KafkaConfig, sources: SourceConfig, keywords: KeywordsConfig)

case class KafkaConfig(bootstrapServers: String)

case class SourceConfig(accepted: List[String], excluded: List[String])

case class KeywordsConfig(
    article: Seq[String],
    articledomains: Seq[String],
    audio: Seq[String],
    discount: Seq[String],
    fkafka: Seq[String],
    job: Seq[String],
    unrelated: Seq[String],
    unrelateddomains: Seq[String],
    version: Seq[String],
    video: Seq[String],
    videodomains: Seq[String]
)
