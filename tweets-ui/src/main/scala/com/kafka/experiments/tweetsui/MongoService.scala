package com.kafka.experiments.tweetsui

import cats.effect.{ContextShift, IO}
import com.kafka.experiments.shared.{
  ArticleTweet,
  AudioTweet,
  DroppedTweet,
  InterestingTweet,
  VersionReleaseTweet
}
import com.kafka.experiments.tweetsui.config.MongodbConfig
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.{Document, MongoClient, MongoCollection, SingleObservable}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.{BsonNumber, BsonString}
import org.mongodb.scala.bson.codecs.Macros._

trait MongoService {
  def interestingTweets(): IO[Seq[InterestingTweet]]

  def interestingTweetsCount(): IO[Long]

  def audioTweets(): IO[Seq[AudioTweet]]

  def articleTweets(): IO[Seq[ArticleTweet]]

  def versionTweets(): IO[Seq[VersionReleaseTweet]]

  def droppedTweets(): IO[Seq[DroppedTweet]]

  def droppedTweetsCount(): IO[Long]

  def delete(category: String, tweetId: Long): IO[Unit]
}

object MongoService {
  def apply(config: MongodbConfig)(implicit c: ContextShift[IO]): MongoService = new DefaultMongoService(config)
}

class DefaultMongoService(config: MongodbConfig)(implicit c: ContextShift[IO]) extends MongoService {

  private val customCodecs = fromProviders(
    classOf[ArticleTweet],
    classOf[AudioTweet],
    classOf[DroppedTweet],
    classOf[InterestingTweet],
    classOf[VersionReleaseTweet]
  )

  private val codecRegistry = fromRegistries(customCodecs, DEFAULT_CODEC_REGISTRY)

  private val mongoClient = MongoClient(s"mongodb://${config.host}:${config.port}")
  private val database = mongoClient.getDatabase(config.tweetsDb).withCodecRegistry(codecRegistry)

  private val collDroppedTweets = database.getCollection(config.collDropped)
  private val collAudioTweets = database.getCollection(config.collAudio)
  private val collArticleTweets = database.getCollection(config.collArticle)
  private val collVersionTweets = database.getCollection(config.collVersion)
  private val collInterestingTweets = database.getCollection(config.collInteresting)

  override def interestingTweets(): IO[Seq[InterestingTweet]] = {
    val tweets = collInterestingTweets.find[InterestingTweet]().limit(50).toFuture()
    IO.fromFuture(IO(tweets))
  }

  override def audioTweets(): IO[Seq[AudioTweet]] = {
    val tweets = collAudioTweets.find[AudioTweet]().limit(50).toFuture()
    IO.fromFuture(IO(tweets))
  }

  override def articleTweets(): IO[Seq[ArticleTweet]] = {
    val tweets = collArticleTweets.find[ArticleTweet]().limit(50).toFuture()
    IO.fromFuture(IO(tweets))
  }

  override def versionTweets(): IO[Seq[VersionReleaseTweet]] = {
    val tweets = collVersionTweets.find[VersionReleaseTweet]().limit(50).toFuture()
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

  override def delete(category: String, tweetId: Long): IO[Unit] = {
    IO.fromFuture(
      IO(collectionFromCategory(category).findOneAndDelete(Document("id" -> BsonNumber(tweetId))).toFuture())
    ).map(_ => ())
  }

  private def collectionFromCategory(category: String): MongoCollection[Document] = {
    category match {
      case DroppedTweet.typeName        => collDroppedTweets
      case InterestingTweet.typeName    => collInterestingTweets
      case AudioTweet.typeName          => collAudioTweets
      case ArticleTweet.typeName        => collArticleTweets
      case VersionReleaseTweet.typeName => collVersionTweets
    }
  }
}
