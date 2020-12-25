package com.kafka.experiments.tweetscategorizer

import com.kafka.experiments.tweetscategorizer.StreamingTopology.topologyBuilder
import com.kafka.experiments.tweetscategorizer.config.GlobalConfig
import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import java.time.Duration
import java.util.Properties

object Main extends App with StrictLogging {
  private val config = ConfigSource.default.loadOrThrow[GlobalConfig]

  val props = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "tweets-categorizer")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.kafka.bootstrapServers)
  props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val streams: KafkaStreams = new KafkaStreams(topologyBuilder().build(), props)
  streams.start()

  sys.ShutdownHookThread {
    streams.close(Duration.ofSeconds(10))
  }
}
