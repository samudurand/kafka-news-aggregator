package com.kafka.experiments.tweetscategorizer

import java.time.Duration
import java.util.Properties

import com.kafka.experiments.shared._
import com.kafka.experiments.tweetscategorizer.KnownSources.hasSourceToBeAutoAccepted
import com.kafka.experiments.tweetscategorizer.Tweet.codec
import com.kafka.experiments.tweetscategorizer.categorize.Categorizer
import com.kafka.experiments.tweetscategorizer.ignore.ToExclude.shouldBeExcluded
import com.kafka.experiments.tweetscategorizer.ignore.ToSkip.{logger, shouldBeSkipped}
import com.typesafe.scalalogging.StrictLogging
import io.circe.Encoder
import io.circe.parser.decode
import io.circe.syntax._
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.scala.ImplicitConversions.{consumedFromSerde, _}
import org.apache.kafka.streams.scala.Serdes.String
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.KStream
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}

object Main extends App with StrictLogging {

  val sourceTopic = "kafka_tweets"
  val sinkArticleTopic = "category_article"
  val sinkAudioTopic = "category_audio"
  val sinkExcludedTopic = "category_excluded"
  val sinkInterestingTopic = "category_interesting"
  val sinkVersionTopic = "category_version"
  val sinkVideoTopic = "category_video"

  val props = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "tweets-categorizer")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val builder: StreamsBuilder = new StreamsBuilder
  val messages: KStream[String, String] = builder.stream[String, String](sourceTopic)

  val tweets = messages.flatMap((key, tweetJson) => parseJsonIntoTweet(key, tweetJson))

  val classifiedTweets = tweets
    .filterNot((_, tweet) => shouldBeSkipped(tweet))
    .mapValues(tweet => {
      if (hasSourceToBeAutoAccepted(tweet)) {
        Categorizer.categorize(tweet)
      } else {
        shouldBeExcluded(tweet) match {
          case Some(reason) =>
            logger.info(s"Tweet should be be excluded for reason [$reason]: $tweet")
            ExcludedTweet(tweet.Id.toString, reason, tweet.Text, tweet.User.ScreenName, tweet.CreatedAt.toString)
          case None => Categorizer.categorize(tweet)
        }
      }
    })

  toTopicByType[ExcludedTweet](classifiedTweets, sinkExcludedTopic)
  toTopicByType[ArticleTweet](classifiedTweets, sinkArticleTopic)
  toTopicByType[AudioTweet](classifiedTweets, sinkAudioTopic)
  toTopicByType[VideoTweet](classifiedTweets, sinkVideoTopic)
  toTopicByType[VersionReleaseTweet](classifiedTweets, sinkVersionTopic)
  toTopicByType[InterestingTweet](classifiedTweets, sinkInterestingTopic)

  val streams: KafkaStreams = new KafkaStreams(builder.build(), props)
  streams.start()

  sys.ShutdownHookThread {
    streams.close(Duration.ofSeconds(10))
  }

  // Using Manifest is not good for performance (Reflection)
  private def toTopicByType[T <: CategorisedTweet: Manifest](
      tweetsStream: KStream[String, CategorisedTweet],
      sinkTopic: String
  )(implicit
      e: Encoder[T]
  ): Unit = {
    tweetsStream
      .flatMap[String, String] {
        case (key, tweet: T) => Some((key, tweet.asJson.noSpaces))
        case _               => None
      }
      .to(sinkTopic)
  }

  private def parseJsonIntoTweet(key: String, tweetJson: String) = {
    logger.info(s"Received tweet: $tweetJson")
    decode[Tweet](tweetJson) match {
      case Right(tweet) => Some((key, tweet))
      case Left(error) =>
        logger.error(s"Unable to parse JSON into Tweet: [$error]")
        None
    }
  }
}
