package com.kafka.experiments.tweetscategorizer

import com.kafka.experiments.tweetscategorizer.StreamingTopology.{
  sinkArticleTopic,
  sinkAudioTopic,
  sinkExcludedTopic,
  sinkInterestingTopic,
  sinkVersionTopic,
  sinkVideoTopic
}
import org.apache.kafka.common.serialization
import org.apache.kafka.common.serialization.{LongSerializer, StringDeserializer, StringSerializer}
import org.apache.kafka.streams.test.OutputVerifier
import org.apache.kafka.streams.{StreamsConfig, TestInputTopic, TestOutputTopic, TopologyTestDriver}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterEach}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.circe.syntax._

import java.util.Properties

class IntegrationTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach {
  val stringSerializer = new StringSerializer()
  val stringDeserializer = new StringDeserializer()
  var testDriver: TopologyTestDriver = _
  var inputTopic: TestInputTopic[String, String] = _
  var outputTopics: Map[String, TestOutputTopic[String, String]] = _

  private val tweet = Tweet(
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

  override def beforeEach(): Unit = {
    val props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");

    testDriver = new TopologyTestDriver(StreamingTopology.topologyBuilder().build(), props);
    inputTopic = createInputTopic()
    outputTopics = createOutputTopics()
  }

  "Categorizer" should "drop an unprocessable message" in {
    inputTopic.pipeInput(tweet.Id.toString, "{}")

    outputTopics.values.foreach(topic => topic.isEmpty shouldBe true)
  }

  it should "drop a tweet not in english" in {
    val tweetNotInEnglish = tweet.copy(Lang = Some("it"))
    inputTopic.pipeInput(tweet.Id.toString, tweetNotInEnglish.asJson.noSpaces)

    outputTopics.values.foreach(topic => topic.isEmpty shouldBe true)
  }

  it should "drop a retweet" in {
    val retweet = tweet.copy(Retweet = true)
    inputTopic.pipeInput(tweet.Id.toString, retweet.asJson.noSpaces)

    outputTopics.values.foreach(topic => topic.isEmpty shouldBe true)
  }

  it should "apply categorization to tweet from known source to be autoaccepted" in {
    val tweetFromConfluent = tweet.copy(Text = "No keyword mention, but from confluent", User = User(12L, "confluentinc"))
    inputTopic.pipeInput(tweet.Id.toString, tweetFromConfluent.asJson.noSpaces)

    outputTopics.view.filterKeys(_ != sinkInterestingTopic).values.foreach(topic => topic.isEmpty shouldBe true)
    outputTopics(sinkInterestingTopic).getQueueSize shouldBe 1
  }

  it should "identify a tweet about a podcast" in {
    val audioTweet = tweet.copy(Text = "Check out this new podcast on Kafka")
    inputTopic.pipeInput(tweet.Id.toString, audioTweet.asJson.noSpaces)

    outputTopics.view.filterKeys(_ != sinkAudioTopic).values.foreach(topic => topic.isEmpty shouldBe true)
    outputTopics(sinkAudioTopic).getQueueSize shouldBe 1
  }

  it should "identify a tweet about a video" in {
    val videoTweet = tweet.copy(Text = "Check out this new video on Kafka")
    inputTopic.pipeInput(tweet.Id.toString, videoTweet.asJson.noSpaces)

    outputTopics.view.filterKeys(_ != sinkVideoTopic).values.foreach(topic => topic.isEmpty shouldBe true)
    outputTopics(sinkVideoTopic).getQueueSize shouldBe 1
  }

  it should "identify a tweet about a new version released" in {
    val versionTweet = tweet.copy(Text = "Check out kafka version 0.11!")
    inputTopic.pipeInput(tweet.Id.toString, versionTweet.asJson.noSpaces)

    outputTopics.view.filterKeys(_ != sinkVersionTopic).values.foreach(topic => topic.isEmpty shouldBe true)
    outputTopics(sinkVersionTopic).getQueueSize shouldBe 1
  }

  it should "identify a tweet about an article" in {
    val articleTweet = tweet.copy(Text = "Check out this tutorial on Kafka!")
    inputTopic.pipeInput(tweet.Id.toString, articleTweet.asJson.noSpaces)

    outputTopics.view.filterKeys(_ != sinkArticleTopic).values.foreach(topic => topic.isEmpty shouldBe true)
    outputTopics(sinkArticleTopic).getQueueSize shouldBe 1
  }

  it should "identify a tweet about anything else interesting" in {
    val otherTweet = tweet.copy(Text = "Something about kafka that should be interesting")
    inputTopic.pipeInput(tweet.Id.toString, otherTweet.asJson.noSpaces)

    outputTopics.view.filterKeys(_ != sinkInterestingTopic).values.foreach(topic => topic.isEmpty shouldBe true)
    outputTopics(sinkInterestingTopic).getQueueSize shouldBe 1
  }

  it should "identify a tweet that should be excluded" in {
    val excludedTweet = tweet.copy(Text = "Should be excluded")
    inputTopic.pipeInput(tweet.Id.toString, excludedTweet.asJson.noSpaces)

    outputTopics.view.filterKeys(_ != sinkExcludedTopic).values.foreach(topic => topic.isEmpty shouldBe true)
    outputTopics(sinkExcludedTopic).getQueueSize shouldBe 1
  }

  private def createInputTopic() = {
    testDriver.createInputTopic("kafka_tweets", stringSerializer, stringSerializer)
  }

  private def createOutputTopics(): Map[String, TestOutputTopic[String, String]] = {
    List(
      sinkArticleTopic,
      sinkAudioTopic,
      sinkInterestingTopic,
      sinkVideoTopic,
      sinkVersionTopic,
      sinkExcludedTopic
    ).map(topic => (topic -> testDriver.createOutputTopic(topic, stringDeserializer, stringDeserializer)))
  }.toMap

  override def afterEach(): Unit = {
    testDriver.close()
  }
}