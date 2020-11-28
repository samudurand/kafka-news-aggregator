package com.kafka.experiments.tweetscategorizer.ignore

import com.kafka.experiments.tweetscategorizer.{Tweet, URLEntity, User}
import com.kafka.experiments.tweetscategorizer.ignore.ToSkip.shouldBeSkipped
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ToSkipTest extends AnyFlatSpec with Matchers {

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

  "A tweet in english" should "not be excluded" in {
    val tweet = goodTweet.copy(Lang = Some("en"))
    shouldBeSkipped(tweet) shouldBe false
  }

  "A tweet in english" should "not be excluded disregarding the casing" in {
    val tweet = goodTweet.copy(Lang = Some("eN"))
    shouldBeSkipped(tweet) shouldBe false
  }

  "A tweet without a language" should "not be excluded" in {
    val tweet = goodTweet.copy(Lang = None)
    shouldBeSkipped(tweet) shouldBe false
  }

  "A tweet not in english" should "be excluded" in {
    val tweet = goodTweet.copy(Lang = Some("fr"))
    shouldBeSkipped(tweet) shouldBe true
  }
}
