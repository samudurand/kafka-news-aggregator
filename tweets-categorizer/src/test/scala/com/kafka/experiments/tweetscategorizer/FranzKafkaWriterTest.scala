package com.kafka.experiments.tweetscategorizer

import com.kafka.experiments.shared.{Tweet, URLEntity, User}
import com.kafka.experiments.tweetscategorizer.ignore.FranzKafkaWriter._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class FranzKafkaWriterTest extends AnyFlatSpec with Matchers {

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

  "Text containing the title of any known book" should "be identified" in {
    val tweet = goodTweet.copy(Text = "The Metamorphosis is a fantastic book!")
    isAboutFranzKafka(tweet) shouldBe true
  }

  "Text not containing the title of any known book" should "not be identified" in {
    val tweet = goodTweet.copy(Text = "The Lord of the Rings is a fantastic book!")
    isAboutFranzKafka(tweet) shouldBe false
  }

  "Text containing the author first name" should "be identified" in {
    val tweet = goodTweet.copy(Text = "The books from Franz kafka are not what we want to find")
    isAboutFranzKafka(tweet) shouldBe true
  }

  "Text not containing the first name of the author" should "not be identified" in {
    val tweet = goodTweet.copy(Text = "Any other tweet should work")
    isAboutFranzKafka(tweet) shouldBe false
  }

  "Text containing a word related to the author" should "be identified" in {
    val tweet = goodTweet.copy(Text = "It's really Kafkaesque!")
    isAboutFranzKafka(tweet) shouldBe true
  }

  "Text not containing a word related to the author" should "not be identified" in {
    val tweet = goodTweet.copy(Text = "Any other tweet should work")
    isAboutFranzKafka(tweet) shouldBe false
  }

  "Tweet mentioning the account of the Kafka author" should "be identified" in {
    val tweet = goodTweet.copy(UserMentionEntities = List(User(12123244L, "kafka")))
    isAboutFranzKafka(tweet) shouldBe true
  }

}
