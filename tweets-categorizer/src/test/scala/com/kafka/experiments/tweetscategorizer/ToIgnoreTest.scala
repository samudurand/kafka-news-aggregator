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

  "A Tweet that doesn't have kafka in the text" should "be ignored" in {
    val tweet = goodTweet.copy(Text = "no mention of keyword")
    shouldBeIgnored(tweet) shouldBe Some(reasonDoesNotMentionKafka)
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
    val tweet = goodTweet.copy(Text = "kafka")
    shouldBeIgnored(tweet) shouldBe Some(reasonIsTooShort)
  }

  "A tweet about a job offer" should "be ignored" in {
    val tweet = goodTweet.copy(Text = "We are hiring kafka devs!")
    shouldBeIgnored(tweet) shouldBe Some(reasonIsAJobOffer)
  }

  "A tweet about a game" should "be ignored" in {
    val tweet = goodTweet.copy(Text = "We just released a great game about Kafka!")
    shouldBeIgnored(tweet) shouldBe Some(reasonIsAboutAGame)
  }

  "A tweet mentioning money with xxx$ format" should "be ignored" in {
    val tweet = goodTweet.copy(Text = "Here is your chance to win 1000$ and spend some on kafka!")
    shouldBeIgnored(tweet) shouldBe Some(reasonIsMoneyRelated)
  }

  "A tweet mentioning money with $xxxx format" should "be ignored" in {
    val tweet = goodTweet.copy(Text = "Here is your chance to win $1000 and spend some on Kafka!")
    shouldBeIgnored(tweet) shouldBe Some(reasonIsMoneyRelated)
  }

  "A tweet about a sale/special deal" should "be ignored" in {
    val tweet = goodTweet.copy(Text = "All courses on Kafka cheaper on Black friday!")
    shouldBeIgnored(tweet) shouldBe Some(reasonIsMoneyRelated)
  }

  "A tweet about a certification" should "be ignored" in {
    val tweet = goodTweet.copy(Text = "The best certification on Kafka is there!")
    shouldBeIgnored(tweet) shouldBe Some(reasonIsAboutACertification)
  }

  "A tweet which contains an ad" should "be ignored" in {
    val tweet = goodTweet.copy(Text = "[Sponsored] Check out that gread deal on Kafka!")
    shouldBeIgnored(tweet) shouldBe Some(reasonIsAnAd)
  }

  "A tweet which has completely unrelated words" should "be ignored" in {
    val tweet = goodTweet.copy(Text = "It's about a Novel on Kafka")
    shouldBeIgnored(tweet) shouldBe Some(reasonHasUnrelatedWords)
  }
}