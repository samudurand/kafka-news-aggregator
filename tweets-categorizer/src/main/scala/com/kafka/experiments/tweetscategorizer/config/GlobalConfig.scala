package com.kafka.experiments.tweetscategorizer.config

case class GlobalConfig(
    dropIfNoLink: Boolean,
    kafka: KafkaConfig,
    sources: SourceConfig,
    keywords: KeywordsConfig,
    redis: RedisConfig
)

case class KafkaConfig(bootstrapServers: String)

case class SourceConfig(accepted: List[String], excluded: List[String], excludedwords: List[String])

case class KeywordsConfig(
    ad: Seq[String],
    article: Seq[String],
    articledomains: Seq[String],
    audio: Seq[String],
    certification: Seq[String],
    discount: Seq[String],
    fkafka: Seq[String],
    job: Seq[String],
    game: Seq[String],
    tagsexclude: Seq[String],
    tool: Seq[String],
    tooldomains: Seq[String],
    version: Seq[String],
    versioncombinations: Seq[String],
    video: Seq[String],
    videodomains: Seq[String],
    otherdomains: Seq[String],
    unrelated: Seq[String],
    unrelateddomains: Seq[String]
)

case class RedisConfig(host: String, port: Int, ttlInHours: Int)
