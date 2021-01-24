package com.kafka.experiments.tweetsui.client

import cats.effect.{ContextShift, IO}
import cats.implicits._
import com.kafka.experiments.shared._
import com.kafka.experiments.tweetsui._
import com.kafka.experiments.tweetsui.client.MongoService._
import com.kafka.experiments.tweetsui.config.MongodbConfig
import com.kafka.experiments.tweetsui.newsletter.{NewsletterTweet, NewsletterTweetDraft}
import com.mongodb.client.model.Filters
import com.typesafe.scalalogging.StrictLogging
import io.circe.Encoder
import io.circe.parser.decode
import io.circe.syntax._
import jdk.jshell.spi.ExecutionControl.NotImplementedException
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Sorts.{descending, orderBy}
import org.mongodb.scala.model.Updates
import org.mongodb.scala.{Document, MongoClient, MongoCollection}

import scala.reflect.ClassTag

trait MongoService {
  def createTweet[T](tweet: T, category: TweetCategory)(implicit encoder: Encoder[T]): IO[Unit]

  def tweets[T](category: TweetCategory)(implicit ct: ClassTag[T]): IO[Seq[T]]

  def tweetsCount(category: TweetCategory): IO[Long]

  def move(sourceColl: String, targetColl: String, tweetId: String): IO[Unit]

  def delete(category: TweetCategory, tweetId: String): IO[Unit]

  def deleteAll(category: TweetCategory): IO[Unit]

  def updateTweetFavourite(tweetId: String, category: TweetCategory, favourite: Boolean): IO[Unit]

  def moveToNewsletter(category: TweetCategory, tweetIds: Seq[String]): IO[Int]

  def changeNewsletterCategory(tweetId: String, category: TweetCategory): IO[Unit]

  def favouriteInNewsletter(tweedId: String, isFavourite: Boolean): IO[Unit]

  def deleteAllInNewsletter(): IO[Unit]

  def deleteInNewsletter(tweetId: String): IO[Unit]

  def tweetsForNewsletter(): IO[Seq[NewsletterTweet]]

  def updateNewsletterTweetScore(tweet: NewsletterTweet): IO[Unit]
}

object MongoService {
  val tweetsDb = "tweets"
  val collArticleName = "article"
  val collAudioName = "audio"
  val collToolName = "tool"
  val collVersionName = "version"
  val collVideoName = "video"
  val collOtherName = "other"
  val collExcludedName = "excluded"
  val collNewsletterName = "newsletter"

  def apply(config: MongodbConfig)(implicit c: ContextShift[IO]): MongoService =
    new DefaultMongoService(MongoClient(s"mongodb://${config.host}:${config.port}"))
}

class DefaultMongoService(mongoClient: MongoClient)(implicit c: ContextShift[IO])
    extends MongoService
    with StrictLogging {

  private val customCodecs = fromProviders(
    classOf[ArticleTweet],
    classOf[AudioTweet],
    classOf[ToolTweet],
    classOf[VideoTweet],
    classOf[ExcludedTweet],
    classOf[OtherTweet],
    classOf[VersionReleaseTweet],
    classOf[NewsletterTweet]
  )

  private val codecRegistry = fromRegistries(customCodecs, DEFAULT_CODEC_REGISTRY)

  private val database = mongoClient.getDatabase(tweetsDb).withCodecRegistry(codecRegistry)

  private val collArticleTweets = database.getCollection(collArticleName)
  private val collAudioTweets = database.getCollection(collAudioName)
  private val collToolTweets = database.getCollection(collToolName)
  private val collVersionTweets = database.getCollection(collVersionName)
  private val collVideoTweets = database.getCollection(collVideoName)
  private val collOtherTweets = database.getCollection(collOtherName)

  private val collExcludedTweets = database.getCollection(collExcludedName)

  private val collNewsletter = database.getCollection(collNewsletterName)

  private val createdAtField = "createdAt"

  override def createTweet[T](tweet: T, category: TweetCategory)(implicit encoder: Encoder[T]): IO[Unit] = {
    val res = collectionFromCategory(category).insertOne(Document(tweet.asJson.noSpaces))
    IO.fromFuture(IO(res.toFuture())).map(_ => ())
  }

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

  override def updateTweetFavourite(tweetId: String, category: TweetCategory, favourite: Boolean): IO[Unit] = {
    val res = collectionFromCategory(category)
      .updateOne(Filters.eq("id", tweetId), Updates.set("favourite", favourite))
    IO.fromFuture(IO(res.toFuture())).map(_ => ())
  }

  private def collectionFromCategory(category: TweetCategory): MongoCollection[Document] = {
    category match {
      case Article        => collArticleTweets
      case Audio          => collAudioTweets
      case Tool           => collToolTweets
      case Excluded       => collExcludedTweets
      case Other          => collOtherTweets
      case VersionRelease => collVersionTweets
      case Video          => collVideoTweets
    }
  }

  override def updateNewsletterTweetScore(tweet: NewsletterTweet): IO[Unit] = {
    val res = collNewsletter.updateOne(Filters.eq("id", tweet.id), Updates.set("score", tweet.score))
    IO.fromFuture(IO(res.toFuture())).map(_ => ())
  }

  override def deleteInNewsletter(tweetId: String): IO[Unit] = {
    IO.fromFuture(
      IO(collNewsletter.deleteOne(Document("id" -> BsonString(tweetId))).toFuture())
    ).map(_ => ())
  }

  override def favouriteInNewsletter(tweetId: String, favourite: Boolean): IO[Unit] = {
    val res = collNewsletter.updateOne(Filters.eq("id", tweetId), Updates.set("favourite", favourite))
    IO.fromFuture(IO(res.toFuture())).map(_ => ())
  }

  override def deleteAllInNewsletter(): IO[Unit] = {
    IO.fromFuture(
      IO(collNewsletter.deleteMany(Document()).toFuture())
    ).map(_ => ())
  }

  override def tweetsForNewsletter(): IO[Seq[NewsletterTweet]] = {
    val tweets = collNewsletter
      .find[NewsletterTweet]()
      .sort(orderBy(descending(createdAtField)))
      .toFuture()
    IO.fromFuture(IO(tweets))
  }

  override def moveToNewsletter(category: TweetCategory, tweetIds: Seq[String]): IO[Int] = {
    tweetIds
      .map(tweetId =>
        collectionFromCategory(category)
          .findOneAndDelete(Document("id" -> BsonString(tweetId)))
          .flatMap(tweetDocument => {
            decode[NewsletterTweetDraft](tweetDocument.toJson()) match {
              case Right(tweet) => {
                val categorisedTweet = NewsletterTweet(category.name, tweet)
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

  override def changeNewsletterCategory(tweetId: String, category: TweetCategory): IO[Unit] = {
    val res = collNewsletter.updateOne(Filters.eq("id", tweetId), Updates.set("category", category.name))
    IO.fromFuture(IO(res.toFuture())).map(_ => ())
  }
}
