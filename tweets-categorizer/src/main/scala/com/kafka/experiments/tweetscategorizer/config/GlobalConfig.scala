package com.kafka.experiments.tweetscategorizer.config

case class GlobalConfig(
    dropIfNoLink: Boolean,
    kafka: KafkaConfig,
    sources: SourceConfig,
    keywords: KeywordsConfig,
    redis: RedisConfig
)

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

case class RedisConfig(host: String, port: Int)
