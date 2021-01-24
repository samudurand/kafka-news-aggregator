package com.kafka.experiments.tweetsui.newsletter

import cats.effect.IO
import com.kafka.experiments.tweetsui.client.MongoService
import com.kafka.experiments.tweetsui.config.NewsletterConfig
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters._

class NewsletterBuilderTest extends AnyFlatSpec with Matchers with MockFactory with BeforeAndAfterEach {

  var fmGenerator: FreeMarkerGenerator = _
  var mongoService: MongoService = _
  var builder: NewsletterBuilder = _

  override def beforeEach(): Unit = {
    fmGenerator = mock[FreeMarkerGenerator]
    mongoService = mock[MongoService]
    builder = new NewsletterBuilder(mongoService, fmGenerator, NewsletterConfig(5))
  }

  "Newsletter Builder" should "build newsletter" in {
    val tweetArticleLowScore = NewsletterTweet(
      "1604688491000",
      "user",
      "Some tweet about #Kafka, http://urltoremove.com",
      "http://url.com",
      "12314513543",
      "article",
      score = 100
    )
    val tweetArticleHighScore = NewsletterTweet(
      "1604688491002",
      "user",
      "Some tweet about #Kafka, http://urltoremove.com",
      "http://url.com",
      "12314513543",
      "article",
      score = 1000
    )
    val tweetArticleFavourite = NewsletterTweet(
      "1604688491002",
      "user",
      "Some tweet about #Kafka, http://urltoremove.com",
      "http://url.com",
      "12314513543",
      "article",
      score = 500,
      favourite = true
    )

    val tweetAudio = NewsletterTweet(
      "1604444491004",
      "user2",
      "Some other tweet http://urltoremove.com about #Kafka",
      "http://url.com",
      "12222213543",
      "audio"
    )

    (mongoService.tweetsForNewsletter _)
      .expects()
      .returning(IO.pure(List(tweetArticleLowScore, tweetArticleHighScore, tweetAudio, tweetArticleFavourite)))

    (fmGenerator.generateHtml _)
      .expects(
        Map(
          "listAudios" -> List(
            tweetAudio.copy(text = "Some other tweet  about #Kafka")
          ).asJava,
          "listArticles" -> List(
            tweetArticleFavourite.copy(text = "Some tweet about #Kafka, "),
            tweetArticleHighScore.copy(text = "Some tweet about #Kafka, "),
            tweetArticleLowScore.copy(text = "Some tweet about #Kafka, ")
          ).asJava
        )
      )
      .returning("generated")

    builder.buildNewsletter().unsafeRunSync() shouldBe "generated"
  }

}
