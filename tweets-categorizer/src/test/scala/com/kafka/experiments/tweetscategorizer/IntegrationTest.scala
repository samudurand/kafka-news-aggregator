package com.kafka.experiments.tweetscategorizer

import com.kafka.experiments.tweetscategorizer.StreamingTopology._
import com.kafka.experiments.tweetscategorizer.categorize.Categorizer
import com.kafka.experiments.tweetscategorizer.ignore.ToSkip
import io.circe.syntax._
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.kafka.streams.{StreamsConfig, TestInputTopic, TestOutputTopic, TopologyTestDriver}
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.Properties

class IntegrationTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach with MockFactory {
  val stringSerializer = new StringSerializer()
  val stringDeserializer = new StringDeserializer()
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
  var testDriver: TopologyTestDriver = _
  var inputTopic: TestInputTopic[String, String] = _
  var outputTopics: Map[String, TestOutputTopic[String, String]] = _
  var categorizer: Categorizer = _
  var redisService: RedisService = _
  var toSkip: ToSkip = _

  override def beforeEach(): Unit = {
    val props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");

    redisService = mock[RedisService]

    categorizer = Categorizer(redisService)
    toSkip = new ToSkip(redisService)
    testDriver = new TopologyTestDriver(StreamingTopology.topologyBuilder(categorizer, toSkip).build(), props);
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
    configureRedisMock()
    val tweetFromConfluent =
      tweet.copy(Text = "No keyword mention, but from confluent", User = User(12L, "confluentinc"))

    inputTopic.pipeInput(tweet.Id.toString, tweetFromConfluent.asJson.noSpaces)

    outputTopics.view.filterKeys(_ != sinkOtherTopic).values.foreach(topic => topic.isEmpty shouldBe true)
    outputTopics(sinkOtherTopic).getQueueSize shouldBe 1
  }

  it should "identify a tweet about a podcast" in {
    configureRedisMock()
    val audioTweet = tweet.copy(Text = "Check out this new podcast on Kafka")
    inputTopic.pipeInput(tweet.Id.toString, audioTweet.asJson.noSpaces)

    outputTopics.view.filterKeys(_ != sinkAudioTopic).values.foreach(topic => topic.isEmpty shouldBe true)
    outputTopics(sinkAudioTopic).getQueueSize shouldBe 1
  }

  it should "identify a tweet about a video" in {
    configureRedisMock()
    val videoTweet = tweet.copy(Text = "Check out this new video on Kafka")
    inputTopic.pipeInput(tweet.Id.toString, videoTweet.asJson.noSpaces)

    outputTopics.view.filterKeys(_ != sinkVideoTopic).values.foreach(topic => topic.isEmpty shouldBe true)
    outputTopics(sinkVideoTopic).getQueueSize shouldBe 1
  }

  it should "identify a tweet about a new version released" in {
    configureRedisMock()
    val versionTweet = tweet.copy(Text = "Check out kafka version 0.11!")
    inputTopic.pipeInput(tweet.Id.toString, versionTweet.asJson.noSpaces)

    outputTopics.view.filterKeys(_ != sinkVersionTopic).values.foreach(topic => topic.isEmpty shouldBe true)
    outputTopics(sinkVersionTopic).getQueueSize shouldBe 1
  }

  it should "identify a tweet about an article" in {
    configureRedisMock()
    val articleTweet = tweet.copy(Text = "Check out this tutorial on Kafka!")
    inputTopic.pipeInput(tweet.Id.toString, articleTweet.asJson.noSpaces)

    outputTopics.view.filterKeys(_ != sinkArticleTopic).values.foreach(topic => topic.isEmpty shouldBe true)
    outputTopics(sinkArticleTopic).getQueueSize shouldBe 1
  }

  it should "identify a tweet about anything else interesting" in {
    configureRedisMock()
    val otherTweet = tweet.copy(Text = "Something about kafka that should be interesting")
    inputTopic.pipeInput(tweet.Id.toString, otherTweet.asJson.noSpaces)

    outputTopics.view.filterKeys(_ != sinkOtherTopic).values.foreach(topic => topic.isEmpty shouldBe true)
    outputTopics(sinkOtherTopic).getQueueSize shouldBe 1
  }

  it should "identify a tweet that should be excluded" in {
    (redisService.exists _).expects(*).returning(false)
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
      sinkOtherTopic,
      sinkVideoTopic,
      sinkVersionTopic,
      sinkExcludedTopic
    ).map(topic => (topic -> testDriver.createOutputTopic(topic, stringDeserializer, stringDeserializer)))
  }.toMap

  override def afterEach(): Unit = {
    testDriver.close()
  }

  private def configureRedisMock() = {
    (redisService.exists _).expects(*).returning(false)
    (redisService.putWithExpire _).expects(*).returning(false)
  }
}
