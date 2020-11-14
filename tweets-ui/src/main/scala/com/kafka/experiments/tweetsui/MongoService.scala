package com.kafka.experiments.tweetsui

import cats.effect.{ContextShift, IO}
import com.kafka.experiments.shared.{DroppedTweet, InterestingTweet}
import com.kafka.experiments.tweetsui.config.MongodbConfig
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._

trait MongoService {
  def interestingTweets(): IO[Seq[InterestingTweet]]
  def interestingTweetsCount(): IO[Long]

  def droppedTweets(): IO[Seq[DroppedTweet]]
  def droppedTweetsCount(): IO[Long]
}

object MongoService {
  def apply(config: MongodbConfig)(implicit c: ContextShift[IO]): MongoService = new DefaultMongoService(config)
}

class DefaultMongoService(config: MongodbConfig)(implicit c: ContextShift[IO]) extends MongoService {

  private val customCodecs = fromProviders(classOf[InterestingTweet], classOf[DroppedTweet])
  private val codecRegistry = fromRegistries(customCodecs, DEFAULT_CODEC_REGISTRY)

  private val mongoClient = MongoClient(s"mongodb://${config.host}:${config.port}")
  private val database = mongoClient.getDatabase(config.tweetsDb).withCodecRegistry(codecRegistry)
  private val collDroppedTweets = database.getCollection(config.collDropped)
  private val collInterestingTweets = database.getCollection(config.collInteresting)

  override def interestingTweets(): IO[Seq[InterestingTweet]] = {
    val tweets = collInterestingTweets.find[InterestingTweet]().limit(50).toFuture()
    IO.fromFuture(IO(tweets))
  }

  override def interestingTweetsCount(): IO[Long] = {
    IO.fromFuture(IO(collInterestingTweets.countDocuments().toFuture()))
  }

  override def droppedTweets(): IO[Seq[DroppedTweet]] = {
    val tweets = collDroppedTweets.find[DroppedTweet]().limit(50).toFuture()
    IO.fromFuture(IO(tweets))
  }

  override def droppedTweetsCount(): IO[Long] = {
    IO.fromFuture(IO(collDroppedTweets.countDocuments().toFuture()))
  }
}
