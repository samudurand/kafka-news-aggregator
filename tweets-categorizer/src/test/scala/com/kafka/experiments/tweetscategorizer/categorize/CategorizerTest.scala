package com.kafka.experiments.tweetscategorizer.categorize

import com.kafka.experiments.tweetscategorizer.{Tweet, URLEntity, User}
import org.scalatest.flatspec.AnyFlatSpec
import Categorizer.{categorize, isAboutAnAudioPost}
import com.kafka.experiments.shared.{ArticleTweet, AudioTweet, DroppedTweet, VersionReleaseTweet}
import org.scalatest.matchers.should.Matchers

class CategorizerTest extends AnyFlatSpec with Matchers {

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

  "A Tweet not mentioning audio keywords" should "not be identified as Audio publication" in {
    val tweet = goodTweet.copy(Text = "check out this written post about Kafka")
    categorize(tweet) should not be an[AudioTweet]
  }

  "A Tweet mentioning audio keywords" should "be identified as Audio publication" in {
    val tweet = goodTweet.copy(Text = "check out this audio post about Kafka")
    categorize(tweet) shouldBe an[AudioTweet]
  }

  "A Tweet mentioning audio but without a link" should "not be identified as Audio publication" in {
    val tweet = goodTweet.copy(Text = "check out this audio post about Kafka", URLEntities = List())
    categorize(tweet) should not be an[AudioTweet]
  }

  "A Tweet mentioning a Version" should "be identified as an announcement about a new Version" in {
    val tweet = goodTweet.copy(Text = "new version 3 available!")
    categorize(tweet) shouldBe a[VersionReleaseTweet]
  }

  "A Tweet mentioning an Article" should "be identified as article related" in {
    val tweet = goodTweet.copy(Text = "great article available!")
    categorize(tweet) shouldBe a[ArticleTweet]
  }

  "A tweet thas has no category nor even a link" should "not be considered interesting" in {
    val tweet = goodTweet.copy(Text = "nothing special", URLEntities = List())
    categorize(tweet) shouldBe a[DroppedTweet]
  }

}
