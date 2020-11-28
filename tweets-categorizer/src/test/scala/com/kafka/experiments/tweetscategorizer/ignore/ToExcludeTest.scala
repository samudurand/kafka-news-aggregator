package com.kafka.experiments.tweetscategorizer.ignore

import com.kafka.experiments.tweetscategorizer.ignore.ToExclude._
import com.kafka.experiments.tweetscategorizer.{Tweet, URLEntity, User}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ToExcludeTest extends AnyFlatSpec with Matchers {

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

  "An interesting Tweet" should "not be excluded" in {
    shouldBeExcluded(goodTweet) shouldBe None
  }

  "A Tweet that doesn't have kafka in the text" should "be excluded" in {
    val tweet = goodTweet.copy(Text = "no mention of keyword")
    shouldBeExcluded(tweet) shouldBe Some(reasonDoesNotMentionKafka)
  }

  "A tweet long enough" should "not be excluded" in {
    val tweet = goodTweet.copy(Text = "Some long enough #kafka text")
    shouldBeExcluded(tweet) shouldBe None
  }

  "A tweet too short" should "be excluded" in {
    val tweet = goodTweet.copy(Text = "kafka")
    shouldBeExcluded(tweet) shouldBe Some(reasonIsTooShort)
  }

  "A tweet about a job offer" should "be excluded" in {
    val tweet = goodTweet.copy(Text = "We are hiring kafka devs!")
    shouldBeExcluded(tweet) shouldBe Some(reasonIsAJobOffer)
  }

  "A tweet about a game" should "be excluded" in {
    val tweet = goodTweet.copy(Text = "We just released a great game about Kafka!")
    shouldBeExcluded(tweet) shouldBe Some(reasonIsAboutAGame)
  }

  "A tweet mentioning money with xxx$ format" should "be excluded" in {
    val tweet = goodTweet.copy(Text = "Here is your chance to win 1000$ and spend some on kafka!")
    shouldBeExcluded(tweet) shouldBe Some(reasonIsMoneyRelated)
  }

  "A tweet mentioning money with $xxxx format" should "be excluded" in {
    val tweet = goodTweet.copy(Text = "Here is your chance to win $1000 and spend some on Kafka!")
    shouldBeExcluded(tweet) shouldBe Some(reasonIsMoneyRelated)
  }

  "A tweet about a sale/special deal" should "be excluded" in {
    val tweet = goodTweet.copy(Text = "All courses on Kafka cheaper on Black friday!")
    shouldBeExcluded(tweet) shouldBe Some(reasonIsMoneyRelated)
  }

  "A tweet about a certification" should "be excluded" in {
    val tweet = goodTweet.copy(Text = "The best certification on Kafka is there!")
    shouldBeExcluded(tweet) shouldBe Some(reasonIsAboutACertification)
  }

  "A tweet which contains an ad" should "be excluded" in {
    val tweet = goodTweet.copy(Text = "[Sponsored] Check out that gread deal on Kafka!")
    shouldBeExcluded(tweet) shouldBe Some(reasonIsAnAd)
  }

  "A tweet which has completely unrelated words" should "be excluded" in {
    val tweet = goodTweet.copy(Text = "It's about a Novel on Kafka")
    shouldBeExcluded(tweet) shouldBe Some(reasonHasUnrelatedWords)
  }
}