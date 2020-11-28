package com.kafka.experiments.tweetsui

import cats.effect.{ContextShift, IO}
import com.kafka.experiments.shared._
import com.kafka.experiments.tweetsui.config.MongodbConfig
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Sorts.{descending, orderBy}
import org.mongodb.scala.{Document, MongoClient, MongoCollection}

import scala.reflect.ClassTag

trait MongoService {

  def tweets[T](category: String)(implicit ct: ClassTag[T]): IO[Seq[T]]

  def tweetsCount(category: String): IO[Long]

  def move(sourceColl: String, targetColl: String, tweetId: String): IO[Unit]

  def delete(category: String, tweetId: String): IO[Unit]
}

object MongoService {
  def apply(config: MongodbConfig)(implicit c: ContextShift[IO]): MongoService = new DefaultMongoService(config)
}

class DefaultMongoService(config: MongodbConfig)(implicit c: ContextShift[IO]) extends MongoService {

  private val customCodecs = fromProviders(
    classOf[ArticleTweet],
    classOf[AudioTweet],
    classOf[VideoTweet],
    classOf[ExcludedTweet],
    classOf[InterestingTweet],
    classOf[VersionReleaseTweet]
  )

  private val codecRegistry = fromRegistries(customCodecs, DEFAULT_CODEC_REGISTRY)

  private val mongoClient = MongoClient(s"mongodb://${config.host}:${config.port}")
  private val database = mongoClient.getDatabase(config.tweetsDb).withCodecRegistry(codecRegistry)

  private val collAudioTweets = database.getCollection(config.collAudio)
  private val collArticleTweets = database.getCollection(config.collArticle)
  private val collInterestingTweets = database.getCollection(config.collInteresting)
  private val collVersionTweets = database.getCollection(config.collVersion)
  private val collVideoTweets = database.getCollection(config.collVideo)

  private val collExcludedTweets = database.getCollection(config.collExcluded)
  private val collExaminateTweets = database.getCollection(config.collInteresting)
  private val collPromotionTweets = database.getCollection(config.collPromotion)

  private val maxResults = 5
  private val createdAtField = "createdAt"

  override def tweets[T](category: String)(implicit ct: ClassTag[T]): IO[Seq[T]] = {
    val tweets = collectionFromCategory(category)
      .find[T]()
      .sort(orderBy(descending(createdAtField)))
      .limit(maxResults)
      .toFuture()
    IO.fromFuture(IO(tweets))
  }

  override def tweetsCount(category: String): IO[Long] = {
    IO.fromFuture(IO(collectionFromCategory(category).countDocuments().toFuture()))
  }

  override def move(sourceColl: String, targetColl: String, tweetId: String): IO[Unit] = {
    val result = collectionFromCategory(sourceColl)
      .findOneAndDelete(Document("id" -> BsonString(tweetId)))
      .map(tweetDocument => collectionFromCategory(targetColl).insertOne(tweetDocument))
    IO.fromFuture(IO(result.toFuture())).map(_ => ())
  }

  override def delete(category: String, tweetId: String): IO[Unit] = {
    IO.fromFuture(
      IO(collectionFromCategory(category).findOneAndDelete(Document("id" -> BsonString(tweetId))).toFuture())
    ).map(_ => ())
  }

  private def collectionFromCategory(category: String): MongoCollection[Document] = {
    category match {
      case ArticleTweet.typeName        => collArticleTweets
      case AudioTweet.typeName          => collAudioTweets
      case ExcludedTweet.typeName       => collExcludedTweets
      case config.collExaminate         => collExaminateTweets
      case InterestingTweet.typeName    => collInterestingTweets
      case config.collPromotion         => collPromotionTweets
      case VersionReleaseTweet.typeName => collVersionTweets
      case VideoTweet.typeName          => collVideoTweets
    }
  }
}
