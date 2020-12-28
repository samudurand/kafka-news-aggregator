package com.kafka.experiments.tweetscategorizer

import com.kafka.experiments.shared._
import com.kafka.experiments.tweetscategorizer.KnownSources.hasSourceToBeAutoAccepted
import com.kafka.experiments.tweetscategorizer.categorize.Categorizer
import com.kafka.experiments.tweetscategorizer.ignore.ToExclude.shouldBeExcluded
import com.kafka.experiments.tweetscategorizer.ignore.ToSkip
import com.typesafe.scalalogging.StrictLogging
import io.circe.Encoder
import io.circe.parser.decode
import io.circe.syntax._
import org.apache.kafka.streams.scala.ImplicitConversions.{consumedFromSerde, _}
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.KStream
import org.apache.kafka.streams.scala.serialization.Serdes.stringSerde

object StreamingTopology extends StrictLogging {
  val sourceTopic = "kafka_tweets"
  val sinkArticleTopic = "category_article"
  val sinkAudioTopic = "category_audio"
  val sinkExcludedTopic = "category_excluded"
  val sinkOtherTopic = "category_other"
  val sinkVersionTopic = "category_version"
  val sinkVideoTopic = "category_video"

  def topologyBuilder(categorizer: Categorizer, toSkip: ToSkip): StreamsBuilder = {

    val builder: StreamsBuilder = new StreamsBuilder
    val messages: KStream[String, String] = builder.stream[String, String](sourceTopic)

    val tweets = messages.flatMap((key, tweetJson) => parseJsonIntoTweet(key, tweetJson))

    val classifiedTweets = tweets
      .filterNot((_, tweet) => toSkip.shouldBeSkipped(tweet))
      .mapValues(tweet => {
        if (hasSourceToBeAutoAccepted(tweet)) {
          categorizer.categorize(tweet)
        } else {
          shouldBeExcluded(tweet) match {
            case Some(reason) =>
              logger.info(s"Tweet should be be excluded for reason [$reason]: $tweet")
              ExcludedTweet(tweet.Id.toString, reason, tweet.Text, tweet.User.ScreenName, tweet.CreatedAt.toString)
            case None => categorizer.categorize(tweet)
          }
        }
      })

    toTopicByType[ExcludedTweet](classifiedTweets, sinkExcludedTopic)
    toTopicByType[ArticleTweet](classifiedTweets, sinkArticleTopic)
    toTopicByType[AudioTweet](classifiedTweets, sinkAudioTopic)
    toTopicByType[VideoTweet](classifiedTweets, sinkVideoTopic)
    toTopicByType[VersionReleaseTweet](classifiedTweets, sinkVersionTopic)
    toTopicByType[OtherTweet](classifiedTweets, sinkOtherTopic)

    builder
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
        case (key, tweet: T) =>
          Some((key, tweet.asJson.noSpaces)) // TODO Try to use a Serde to handle JSON directly !!!!!!!
        case _ => None
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
