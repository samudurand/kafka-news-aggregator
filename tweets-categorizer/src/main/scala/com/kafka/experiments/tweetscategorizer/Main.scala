package com.kafka.experiments.tweetscategorizer

import java.time.Duration
import java.util.Properties

import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.streams.scala.ImplicitConversions.consumedFromSerde
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.Serdes._
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.KStream
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import io.circe.parser.decode
import Tweet.codec
import com.kafka.experiments.shared.{DroppedTweet, InterestingTweet}
import com.kafka.experiments.tweetscategorizer.ignore.ToIgnore.shouldBeIgnored
import io.circe.syntax._

object Main extends App with StrictLogging {

  val sourceTopic = "kafka_tweets"
  val sinkDroppedTopic = "dropped_tweets"
  val sinkInterestingTopic = "interesting_tweets"

  val props = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "tweets-categorizer")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")

  val builder: StreamsBuilder = new StreamsBuilder
  val messages: KStream[String, String] = builder.stream[String, String](sourceTopic)

  val tweets = messages.flatMap((key, tweetJson) => parseJsonIntoTweet(key, tweetJson))
  val classifiedTweets = tweets.mapValues(tweet => {
    shouldBeIgnored(tweet) match {
      case Some(reason) => DroppedTweet(tweet.Text, tweet.Retweet, reason, tweet.User.ScreenName)
      case None => InterestingTweet(tweet.Text, tweet.Retweet, tweet.User.ScreenName)
    }
  })

  classifiedTweets
    .flatMap[String, String] {
      case (key, tweet: DroppedTweet) => Some((key, tweet.asJson.noSpaces))
      case _ => None
    }
    .to(sinkDroppedTopic)

  classifiedTweets
    .flatMap[String, String] {
      case (key, tweet: InterestingTweet) => Some((key, tweet.asJson.noSpaces))
      case _ => None
    }
    .to(sinkInterestingTopic)

  val streams: KafkaStreams = new KafkaStreams(builder.build(), props)
  streams.start()

  private def parseJsonIntoTweet(key: String, tweetJson: String) = {
    println(tweetJson)
    decode[Tweet](tweetJson) match {
      case Right(tweet) => Some((key, tweet))
      case Left(error) =>
        logger.error(s"Unable to parse JSON into Tweet: [$error]")
        None
    }
  }

  sys.ShutdownHookThread {
    streams.close(Duration.ofSeconds(10))
  }
}
