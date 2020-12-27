package com.kafka.experiments.tweetscategorizer.ignore

import com.kafka.experiments.tweetscategorizer.{RedisService, Tweet, URLEntity, User}
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ToSkipTest extends AnyFlatSpec with Matchers with MockFactory with BeforeAndAfterEach {

  private val goodTweet = Tweet(
    1604688491000L,
    1324785668502016000L,
    "Some tweet about #Kafka",
    0L,
    Retweet = false,
    0L,
    Some("en"),
    List(URLEntity("https://t.co/0lztrRpQTK", "http://google.com")),
    List(),
    User(1234124134L, "someuser")
  )

  private var redisService: RedisService = _
  private var service: ToSkip = _

  override def beforeEach: Unit = {
    redisService = mock[RedisService]
    service = new ToSkip(redisService)
  }

  "A tweet in english" should "not be skipped" in {
    val tweet = goodTweet.copy(Lang = Some("en"))
    (redisService.exists _).expects(*).returning(false)

    service.shouldBeSkipped(tweet) shouldBe false
  }

  "A tweet in english" should "not be skipped disregarding the casing" in {
    val tweet = goodTweet.copy(Lang = Some("eN"))
    (redisService.exists _).expects(*).returning(false)

    service.shouldBeSkipped(tweet) shouldBe false
  }

  "A tweet without a language" should "not be skipped" in {
    val tweet = goodTweet.copy(Lang = None)
    (redisService.exists _).expects(*).returning(false)

    service.shouldBeSkipped(tweet) shouldBe false
  }

  "A tweet not in english" should "be skipped" in {
    val tweet = goodTweet.copy(Lang = Some("fr"))
    service.shouldBeSkipped(tweet) shouldBe true
  }

  "A tweet with a known URL" should "be skipped" in {
    (redisService.exists _).expects(*).returning(true)

    service.shouldBeSkipped(goodTweet) shouldBe true
  }
}
