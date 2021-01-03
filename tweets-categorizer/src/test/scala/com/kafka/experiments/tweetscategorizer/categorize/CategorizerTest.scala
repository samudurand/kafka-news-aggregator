package com.kafka.experiments.tweetscategorizer.categorize

import com.kafka.experiments.shared.{
  ArticleTweet,
  AudioTweet,
  ExcludedTweet,
  OtherTweet,
  VersionReleaseTweet,
  VideoTweet
}
import com.kafka.experiments.tweetscategorizer.config.RedisConfig
import com.kafka.experiments.tweetscategorizer.{RedisService, Tweet, URLEntity, User}
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CategorizerTest extends AnyFlatSpec with Matchers with MockFactory with BeforeAndAfterEach {

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
  private var categorizer: Categorizer = _

  override def beforeEach: Unit = {
    redisService = mock[RedisService]
    categorizer = Categorizer(redisService)
  }

  "A Tweet not mentioning audio keywords" should "not be identified as Audio publication" in {
    (redisService.putWithExpire _).expects(*).returning(false)
    val tweet = goodTweet.copy(Text = "check out this written post about Kafka")

    categorizer.categorize(tweet) shouldBe an[OtherTweet]
  }

  "A Tweet mentioning audio keywords" should "be identified as Audio publication" in {
    (redisService.putWithExpire _).expects(*).returning(false)
    val tweet = goodTweet.copy(Text = "check out this audio post about Kafka")

    categorizer.categorize(tweet) shouldBe an[AudioTweet]
  }

  "A Tweet mentioning audio but without a link" should "not be identified as Audio publication" in {
    val tweet = goodTweet.copy(Text = "check out this audio post about Kafka", URLEntities = List())
    categorizer.categorize(tweet) should not be an[AudioTweet]
  }

  "A Tweet mentioning video keywords" should "be identified as Video publication" in {
    (redisService.putWithExpire _).expects(*).returning(false)
    val tweet = goodTweet.copy(Text = "check out this video post about Kafka")

    categorizer.categorize(tweet) shouldBe an[VideoTweet]
  }

  "A Tweet with a video link" should "be identified as Video publication" in {
    (redisService.putWithExpire _).expects(*).returning(false)
    val tweet = goodTweet.copy(URLEntities = List(URLEntity("https://sdfs.com", "https://youtube.com/sdf")))

    categorizer.categorize(tweet) shouldBe an[VideoTweet]
  }

  "A Tweet mentioning a Version" should "be identified as an announcement about a new Version" in {
    (redisService.putWithExpire _).expects(*).returning(false)
    val tweet = goodTweet.copy(Text = "new version 3 available!")

    categorizer.categorize(tweet) shouldBe a[VersionReleaseTweet]
  }

  "A Tweet mentioning with a link containing a number" should "not be identified as a new Version" in {
    (redisService.putWithExpire _).expects(*).returning(false)
    val tweet = goodTweet.copy(Text = "new version https://stack.com/34/link")

    categorizer.categorize(tweet) shouldBe a[OtherTweet]
  }

  "A Tweet mentioning an Article" should "be identified as article related" in {
    (redisService.putWithExpire _).expects(*).returning(false)
    val tweet = goodTweet.copy(Text = "great article available!")

    categorizer.categorize(tweet) shouldBe a[ArticleTweet]
  }

  "A Tweet containing a link to a known article domain" should "be identified as article related" in {
    (redisService.putWithExpire _).expects(*).returning(false)
    val tweet = goodTweet.copy(URLEntities = List(URLEntity("http://tinylink.com", "https://dzone.com/some/article")))

    categorizer.categorize(tweet) shouldBe a[ArticleTweet]
  }

  "A tweet thas has no category nor even a link" should "not be considered interesting" in {
    val tweet = goodTweet.copy(Text = "nothing special", URLEntities = List())
    categorizer.categorize(tweet) shouldBe a[ExcludedTweet]
  }

}
