package com.kafka.experiments.tweetscategorizer

import com.kafka.experiments.tweetscategorizer.KnownSources._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class KnownSourcesTest extends AnyFlatSpec with Matchers {

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
    User(123414114423L, "someuser")
  )

  "A Tweet from a random source" should "not be dropped" in {
    val tweet = goodTweet.copy(User = User(123131413L, "someguy"))
    hasSourceToBeDropped(tweet) shouldBe false
  }

  "A Tweet from a source that should be dropped" should "be identified" in {
    val tweet = goodTweet.copy(User = User(123131413L, "functionalworks"))
    hasSourceToBeDropped(tweet) shouldBe true
  }

}
