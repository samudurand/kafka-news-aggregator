package com.kafka.experiments.tweetscategorizer

import com.kafka.experiments.tweetscategorizer.tweetUtils.hasLink
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class tweetUtilsTest extends AnyFlatSpec with Matchers {

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

  "Tweet with a link" should "be identified" in {
    hasLink(goodTweet) shouldBe true
  }

  "Tweet without a link" should "be identified" in {
    val tweet = goodTweet.copy(URLEntities = List())
    hasLink(tweet) shouldBe false
  }

  "Tweet with only a twitter link" should "be identified" in {
    val tweet = goodTweet.copy(URLEntities = List(URLEntity("https://t.co/0lztrRpQTK", "https://twitter.com/some/tweet")))
    hasLink(tweet) shouldBe false
  }

}
