package com.kafka.experiments.tweetsui

import cats.effect.{ContextShift, IO}
import com.kafka.experiments.shared._
import com.kafka.experiments.tweetsui.config.MongodbConfig
import com.kafka.experiments.tweetsui.newsletter.{CompleteNewsletterTweet, NewsletterTweet}
import com.typesafe.scalalogging.StrictLogging
import jdk.jshell.spi.ExecutionControl.NotImplementedException
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Sorts.{descending, orderBy}
import org.mongodb.scala.{Document, MongoClient, MongoCollection}

import scala.reflect.ClassTag

trait MongoService {
  def tweets[T](category: TweetCategory)(implicit ct: ClassTag[T]): IO[Seq[T]]

  def tweetsCount(category: TweetCategory): IO[Long]

  def move(sourceColl: String, targetColl: String, tweetId: String): IO[Unit]

  def delete(category: TweetCategory, tweetId: String): IO[Unit]

  def deleteAll(category: TweetCategory): IO[Unit]

  def moveToNewsletter(category: TweetCategory, tweetIds: Seq[String]): IO[Int]

  def deleteAllInNewsletter(): IO[Unit]

  def tweetsForNewsletter(): IO[Seq[CompleteNewsletterTweet]]
}

object MongoService {

  def apply(config: MongodbConfig)(implicit c: ContextShift[IO]): MongoService =
    new DefaultMongoService(config, MongoClient(s"mongodb://${config.host}:${config.port}"))
}

class DefaultMongoService(config: MongodbConfig, mongoClient: MongoClient)(implicit c: ContextShift[IO])
    extends MongoService
    with StrictLogging {

  private val customCodecs = fromProviders(
    classOf[ArticleTweet],
    classOf[AudioTweet],
    classOf[VideoTweet],
    classOf[ExcludedTweet],
    classOf[OtherTweet],
    classOf[VersionReleaseTweet],
    classOf[CompleteNewsletterTweet]
  )

  private val codecRegistry = fromRegistries(customCodecs, DEFAULT_CODEC_REGISTRY)

  private val database = mongoClient.getDatabase(config.tweetsDb).withCodecRegistry(codecRegistry)

  private val collAudioTweets = database.getCollection(config.collAudio)
  private val collArticleTweets = database.getCollection(config.collArticle)
  private val collInterestingTweets = database.getCollection(config.collInteresting)
  private val collVersionTweets = database.getCollection(config.collVersion)
  private val collVideoTweets = database.getCollection(config.collVideo)

  private val collExcludedTweets = database.getCollection(config.collExcluded)
  private val collExaminateTweets = database.getCollection(config.collInteresting)
  private val collPromotionTweets = database.getCollection(config.collPromotion)

  private val collNewsletter = database.getCollection(config.collNewsletter)

  private val createdAtField = "createdAt"

  override def tweets[T](category: TweetCategory)(implicit ct: ClassTag[T]): IO[Seq[T]] = {
    val tweets = collectionFromCategory(category)
      .find[T]()
      .sort(orderBy(descending(createdAtField)))
      .toFuture()
    IO.fromFuture(IO(tweets))
  }

  override def tweetsCount(category: TweetCategory): IO[Long] = {
    IO.fromFuture(IO(collectionFromCategory(category).countDocuments().toFuture()))
  }

  override def move(sourceColl: String, targetColl: String, tweetId: String): IO[Unit] = {
    throw new NotImplementedException("This feature needs fixing")
    //    val result = collectionFromCategory(sourceColl)
    //      .findOneAndDelete(Document("id" -> BsonString(tweetId)))
    //      .map(tweetDocument => collectionFromCategory(targetColl).insertOne(tweetDocument))
    //    IO.fromFuture(IO(result.toFuture())).map(_ => ())
  }

  override def delete(category: TweetCategory, tweetId: String): IO[Unit] = {
    IO.fromFuture(
      IO(collectionFromCategory(category).findOneAndDelete(Document("id" -> BsonString(tweetId))).toFuture())
    ).map(_ => ())
  }

  override def deleteAll(category: TweetCategory): IO[Unit] = {
    IO.fromFuture(
      IO(collectionFromCategory(category).deleteMany(Document()).toFuture())
    ).map(_ => ())
  }

  override def deleteAllInNewsletter(): IO[Unit] = {
    IO.fromFuture(
      IO(collNewsletter.deleteMany(Document()).toFuture())
    ).map(_ => ())
  }

  override def tweetsForNewsletter(): IO[Seq[CompleteNewsletterTweet]] = {
    val tweets = collNewsletter
      .find[CompleteNewsletterTweet]()
      .sort(orderBy(descending(createdAtField)))
      .toFuture()
    IO.fromFuture(IO(tweets))
  }

  override def moveToNewsletter(category: TweetCategory, tweetIds: Seq[String]): IO[Int] = {
    import cats.implicits._
    import io.circe.parser.decode
    import io.circe.syntax._

    tweetIds
      .map(tweetId =>
        collectionFromCategory(category)
          .findOneAndDelete(Document("id" -> BsonString(tweetId)))
          .flatMap(tweetDocument => {
            decode[NewsletterTweet](tweetDocument.toJson()) match {
              case Right(tweet) => {
                val categorisedTweet = CompleteNewsletterTweet(category.name, tweet)
                val document = Document.apply(categorisedTweet.asJson.toString())
                collNewsletter.insertOne(document)
              }
              // TODO improve, currently the tweet is lost if something goes wrong
              case Left(error) =>
                throw new RuntimeException(s"Unable to move tweet [$tweetDocument], tweet has been lost: $error")
            }
          })
      )
      .map(result => IO.fromFuture(IO(result.toFuture())))
      .toList
      .sequence
      .map(tweets => {
        val count = tweets.map(_.count(_.wasAcknowledged())).sum
        logger.info(s"Moved $count tweets from category $category to the newsletter")
        count
      }) // No special handling of failures for now
  }

  private def collectionFromCategory(category: TweetCategory): MongoCollection[Document] = {
    category match {
      case Article        => collArticleTweets
      case Audio          => collAudioTweets
      case Excluded       => collExcludedTweets
      case Other          => collInterestingTweets
      case VersionRelease => collVersionTweets
      case Video          => collVideoTweets
    }
  }
}
