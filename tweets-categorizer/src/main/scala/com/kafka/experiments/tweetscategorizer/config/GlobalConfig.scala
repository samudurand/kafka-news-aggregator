package com.kafka.experiments.tweetscategorizer.config

case class GlobalConfig(sources: SourceConfig)

case class SourceConfig(ignored: List[String])
