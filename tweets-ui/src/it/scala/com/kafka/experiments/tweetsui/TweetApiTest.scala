package com.kafka.experiments.tweetsui

import cats.effect.IO
import com.dimafeng.testcontainers.ForEachTestContainer
import com.kafka.experiments.shared.{ArticleTweet, AudioTweet, VersionReleaseTweet, VideoTweet}
import com.kafka.experiments.tweetsui.Decoders._
import com.kafka.experiments.tweetsui.Encoders._
import com.kafka.experiments.tweetsui.api.{CountResult, TweetApi, UpdateTweet}
import org.http4s._
import org.http4s.implicits.{http4sLiteralsSyntax, _}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TweetApiTest
    extends AnyFlatSpec
    with ForEachTestContainer
    with BeforeAndAfterEach
    with Matchers
    with MongoDatabase {

  private var api: HttpApp[IO] = _

  override def beforeEach(): Unit = {
    api = new TweetApi(mongoService).api().orNotFound
  }

  "Tweet API" should "retrieve tweets in category Article" in {
    val tweet = ArticleTweet("124142314", "Some good Kafka stuff", "http://medium.com/123445", "mlmenace", "1609020620")
    val tweet2 = ArticleTweet("124142334", "Even move Kafka stuff", "http://medium.com/789445", "justin", "1605020620")
    mongoService.createTweet(tweet, Article).unsafeRunSync()
    mongoService.createTweet(tweet2, Article).unsafeRunSync()

    val response = api.run(Request(method = Method.GET, uri = uri"/tweets/article"))

    check[Seq[ArticleTweet]](response, Status.Ok, Some(List(tweet, tweet2)))
  }

  "Tweet API" should "retrieve tweet count in category Audio" in {
    val tweet = AudioTweet("124142314", "Some good Kafka stuff", "http://medium.com/123445", "mlmenace", "1609020620")
    val tweet2 = AudioTweet("124142334", "Even move Kafka stuff", "http://medium.com/789445", "justin", "1605020620")
    mongoService.createTweet(tweet, Audio).unsafeRunSync()
    mongoService.createTweet(tweet2, Audio).unsafeRunSync()

    val response = api.run(Request(method = Method.GET, uri = uri"/tweets/audio/count"))

    check(response, Status.Ok, Some(CountResult(2)))
  }

  "Tweet API" should "delete one tweet in category video" in {
    val tweet = VideoTweet("124142314", "Some good Kafka stuff", "http://medium.com/123445", "mlmenace", "1609020620")
    val tweet2 = VideoTweet("124142334", "Even move Kafka stuff", "http://medium.com/789445", "justin", "1605020620")
    mongoService.createTweet(tweet, Video).unsafeRunSync()
    mongoService.createTweet(tweet2, Video).unsafeRunSync()

    val response = api.run(Request(method = Method.DELETE, uri = uri"/tweets/video/124142314"))

    check(response, Status.Ok, Some("Deleted"))
    mongoService.tweetsCount(Video).unsafeRunSync() shouldBe 1
  }

  "Tweet API" should "set tweet as favourite" in {
    val tweet = VideoTweet("124142314", "Some good Kafka stuff", "http://medium.com/123445", "mlmenace", "1609020620")
    mongoService.createTweet(tweet, Video).unsafeRunSync()

    val response = api.run(Request(method = Method.PUT, uri = uri"/tweets/video/124142314")
      .withEntity(UpdateTweet(true)))

    check(response, Status.Ok, Some("Updated"))
    val response2 = api.run(Request(method = Method.GET, uri = uri"/tweets/video"))
    check[Seq[VideoTweet]](response2, Status.Ok, Some(List(
      VideoTweet("124142314", "Some good Kafka stuff", "http://medium.com/123445", "mlmenace", "1609020620", favourite = true)
    )))
  }

  "Tweet API" should "delete all tweets in category version release" in {
    val tweet = VersionReleaseTweet("124142314", "Some good Kafka stuff", "http://med/123445", "mlmenace", "1609020620")
    val tweet2 = VersionReleaseTweet("124142334", "Even move Kafka stuff", "http://mediu/78944", "justin", "1605020620")
    mongoService.createTweet(tweet, VersionRelease).unsafeRunSync()
    mongoService.createTweet(tweet2, VersionRelease).unsafeRunSync()

    val response = api.run(Request(method = Method.DELETE, uri = uri"/tweets/version"))

    check(response, Status.Ok, Some("All tweets in category VersionRelease deleted"))
    mongoService.tweetsCount(VersionRelease).unsafeRunSync() shouldBe 0
  }

  def check[A](actual: IO[Response[IO]], expectedStatus: Status, expectedBody: Option[A])(implicit
      ev: EntityDecoder[IO, A]
  ): Unit = {
    val actualResp = actual.unsafeRunSync()
    actualResp.status shouldBe expectedStatus
    expectedBody.foreach(expected => actualResp.as[A].unsafeRunSync() shouldBe expected)
  }

}
