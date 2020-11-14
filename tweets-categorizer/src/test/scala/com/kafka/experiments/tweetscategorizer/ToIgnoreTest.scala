package com.kafka.experiments.tweetscategorizer

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import com.kafka.experiments.tweetscategorizer.ignore.ToIgnore._

class ToIgnoreTest extends AnyFlatSpec with Matchers {

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

  "An interesting Tweet" should "not be ignored" in {
    shouldBeIgnored(goodTweet) shouldBe None
  }

  "A Retweet" should "be ignored" in {
    val tweet = goodTweet.copy(Retweet = true)
    shouldBeIgnored(tweet) shouldBe Some(reasonIsRetweet)
  }

  "A tweet in english" should "not be ignored" in {
    val tweet = goodTweet.copy(Lang = Some("en"))
    shouldBeIgnored(tweet) shouldBe None
  }

  "A tweet in english" should "not be ignored disregarding the casing" in {
    val tweet = goodTweet.copy(Lang = Some("eN"))
    shouldBeIgnored(tweet) shouldBe None
  }

  "A tweet without a language" should "be ignored" in {
    val tweet = goodTweet.copy(Lang = None)
    shouldBeIgnored(tweet) shouldBe Some(reasonIsNotInEnglish)
  }

  "A tweet not in english" should "be ignored" in {
    val tweet = goodTweet.copy(Lang = Some("fr"))
    shouldBeIgnored(tweet) shouldBe Some(reasonIsNotInEnglish)
  }

  "A tweet long enough" should "not be ignored" in {
    val tweet = goodTweet.copy(Text = "Some long enough #kafka text")
    shouldBeIgnored(tweet) shouldBe None
  }

  "A tweet too short" should "be ignored" in {
    val tweet = goodTweet.copy(Text = "too short")
    shouldBeIgnored(tweet) shouldBe Some(reasonIsTooShort)
  }

  "A tweet about a job offer" should "be ignored" in {
    val tweet = goodTweet.copy(Text = "We are hiring!")
    shouldBeIgnored(tweet) shouldBe Some(reasonIsAJobOffer)
  }

  "A tweet about a game" should "be ignored" in {
    val tweet = goodTweet.copy(Text = "We just released a great game!")
    shouldBeIgnored(tweet) shouldBe Some(reasonIsAboutAGame)
  }

  "A tweet mentioning money with xxx$ format" should "be ignored" in {
    val tweet = goodTweet.copy(Text = "Here is your chance to win 1000$!")
    shouldBeIgnored(tweet) shouldBe Some(reasonIsMoneyRelated)
  }

  "A tweet mentioning money with $xxxx format" should "be ignored" in {
    val tweet = goodTweet.copy(Text = "Here is your chance to win $1000!")
    shouldBeIgnored(tweet) shouldBe Some(reasonIsMoneyRelated)
  }

  "A tweet about a certification" should "be ignored" in {
    val tweet = goodTweet.copy(Text = "The best certification on Kafka is there!")
    shouldBeIgnored(tweet) shouldBe Some(reasonIsAboutACertification)
  }

  "A tweet which contains an ad" should "be ignored" in {
    val tweet = goodTweet.copy(Text = "[Sponsored] Check out that gread deal!")
    shouldBeIgnored(tweet) shouldBe Some(reasonIsAnAd)
  }
}