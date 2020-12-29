package com.kafka.experiments.tweetsui

import cats.effect.IO
import com.dimafeng.testcontainers.ForEachTestContainer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.kafka.experiments.shared.{ArticleTweet, AudioTweet}
import com.kafka.experiments.tweetsui.Decoders._
import com.kafka.experiments.tweetsui.Encoders._
import com.kafka.experiments.tweetsui.config.SendGridConfig
import com.kafka.experiments.tweetsui.newsletter.NewsletterTweet
import com.kafka.experiments.tweetsui.sendgrid.SendGridClient
import org.http4s._
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits.{http4sLiteralsSyntax, _}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.UUID
import scala.concurrent.ExecutionContext.global

class NewsletterApiTest
    extends AnyFlatSpec
    with ForEachTestContainer
    with BeforeAndAfterEach
    with Matchers
    with MongoDatabase
    with MockSendGrid {

  private val sendGridConfig = SendGridConfig(mockSendGridUrl, "key", 11, List("id"), 22)
  private var httpClient: Client[IO] = _
  private var sendGridClient: SendGridClient = _
  private var api: HttpApp[IO] = _

  override def beforeEach: Unit = {
    super.beforeEach()
    httpClient = BlazeClientBuilder[IO](global).allocated.unsafeRunSync()._1
    sendGridClient = SendGridClient(sendGridConfig, httpClient)
    api = Main.api(sendGridClient).orNotFound
  }

  "Newsletter API" should "be used to prepare and create the email draft in SendGrid" in {
    val tweet = ArticleTweet("124142314", "Some good Kafka stuff", "http://medium.com/123445", "mlmenace", "1609020620")
    val tweet2 = AudioTweet("124142334", "Even move Kafka stuff", "http://medium.com/789445", "justin", "1605020620")
    mongoService.createTweet(tweet, Article).unsafeRunSync()
    mongoService.createTweet(tweet2, Audio).unsafeRunSync()
    val tweetsToInclude = MoveTweetsToNewsletter(
      Map(
        "article" -> List("124142314"),
        "audio" -> List("124142334")
      )
    )

    // Prepare the tweets that should be included in the Newsletter
    val response1 = api.run(Request(method = Method.PUT, uri = uri"/newsletter/prepare").withEntity(tweetsToInclude))
    check[String](response1, Status.Ok, None)

    // Check the newsletter content
    val response2 = api.run(Request(method = Method.GET, uri = uri"/newsletter/included"))
    check[Seq[NewsletterTweet]](
      response2,
      Status.Ok,
      Some(
        List(
          NewsletterTweet(
            "124142314",
            "mlmenace",
            "Some good Kafka stuff",
            "http://medium.com/123445",
            "1609020620",
            "article"
          ),
          NewsletterTweet(
            "124142334",
            "justin",
            "Even move Kafka stuff",
            "http://medium.com/789445",
            "1605020620",
            "audio"
          )
        )
      )
    )

    // Check the Newsletter appearance
    val response3 = api.run(Request(method = Method.GET, uri = uri"/newsletter/html"))
    val htmlContent = check[String](response3, Status.Ok, None).as[String].unsafeRunSync()
    htmlContent should include("Some good Kafka stuff")
    htmlContent should include("Even move Kafka stuff")

    // Create Email draft
    wireMockServer.stubFor(
      post(urlPathEqualTo("/v3/marketing/singlesends"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBody(s"""{"id":"${UUID.randomUUID()}"}""")
        )
    )
    val response4 = api.run(Request(method = Method.POST, uri = uri"/newsletter/create"))
    check[String](response4, Status.Ok, None)
    wireMockServer.verify(
      postRequestedFor(urlEqualTo("/v3/marketing/singlesends")).withHeader("Content-Type", equalTo("application/json"))
    )
  }

  "Newsletter API" should "reset newsletter data" in {
    val tweet = ArticleTweet("124142314", "Some good Kafka stuff", "http://medium.com/123445", "mlmenace", "1609020620")
    val tweet2 = AudioTweet("124142334", "Even move Kafka stuff", "http://medium.com/789445", "justin", "1605020620")
    mongoService.createTweet(tweet, Article).unsafeRunSync()
    mongoService.createTweet(tweet2, Audio).unsafeRunSync()
    val tweetsToInclude = MoveTweetsToNewsletter(
      Map(
        "article" -> List("124142314"),
        "audio" -> List("124142334")
      )
    )

    val response1 = api.run(Request(method = Method.PUT, uri = uri"/newsletter/prepare").withEntity(tweetsToInclude))
    check[String](response1, Status.Ok, None)
    val response2 = api.run(Request(method = Method.DELETE, uri = uri"/newsletter/reset"))
    check[String](response2, Status.Ok, None)
    val response3 = api.run(Request(method = Method.GET, uri = uri"/newsletter/included"))
    check[Seq[NewsletterTweet]](response3, Status.Ok, Some(List[NewsletterTweet]()))

    verifyZeroInteractionsWithSendGridServer()
  }

  "Newsletter API" should "delete a specifc tweet in newsletter" in {
    val tweet = ArticleTweet("124142314", "Some good Kafka stuff", "http://medium.com/123445", "mlmenace", "1609020620")
    val tweet2 = AudioTweet("124142334", "Even move Kafka stuff", "http://medium.com/789445", "justin", "1605020620")
    mongoService.createTweet(tweet, Article).unsafeRunSync()
    mongoService.createTweet(tweet2, Audio).unsafeRunSync()
    val tweetsToInclude = MoveTweetsToNewsletter(
      Map(
        "article" -> List("124142314"),
        "audio" -> List("124142334")
      )
    )

    val response1 = api.run(Request(method = Method.PUT, uri = uri"/newsletter/prepare").withEntity(tweetsToInclude))
    check[String](response1, Status.Ok, None)
    val response2 = api.run(Request(method = Method.DELETE, uri = uri"/newsletter/tweet/124142314"))
    check[String](response2, Status.Ok, None)
    val response3 = api.run(Request(method = Method.GET, uri = uri"/newsletter/included"))
    check[Seq[NewsletterTweet]](
      response3,
      Status.Ok,
      Some(
        List[NewsletterTweet](
          NewsletterTweet(
            "124142334",
            "justin",
            "Even move Kafka stuff",
            "http://medium.com/789445",
            "1605020620",
            "audio"
          )
        )
      )
    )
  }

  def check[A](actual: IO[Response[IO]], expectedStatus: Status, expectedBody: Option[A])(implicit
      ev: EntityDecoder[IO, A]
  ): Response[IO] = {
    val actualResp = actual.unsafeRunSync()
    actualResp.status shouldBe expectedStatus
    expectedBody.foreach(expected => actualResp.as[A].unsafeRunSync() shouldBe expected)
    actualResp
  }

}
