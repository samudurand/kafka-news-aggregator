package com.kafka.experiments.tweetsui

import cats.effect.{ContextShift, IO}
import com.dimafeng.testcontainers.{FixedHostPortGenericContainer, ForEachTestContainer}
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, urlPathEqualTo}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.kafka.experiments.shared.ArticleTweet
import com.kafka.experiments.tweetsui.Decoders._
import com.kafka.experiments.tweetsui.config.SendGridConfig
import com.kafka.experiments.tweetsui.sendgrid.SendGridClient
import org.http4s._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits.{http4sLiteralsSyntax, _}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.global

class NewsletterApiTest extends AnyFlatSpec with ForEachTestContainer with BeforeAndAfterEach with Matchers {
  implicit val contextShift: ContextShift[IO] = IO.contextShift(global)

  override val container = new FixedHostPortGenericContainer(
    "mongo:4.4.2",
    exposedContainerPort = 27017,
    exposedHostPort = 28017
  )

  private val sendGridConfig = SendGridConfig("http://localhost:4000", "key", 11, List("id"), 22)

  "Tweet API" should "retrieve tweets in category Article" in {
    val httpClient = BlazeClientBuilder[IO](global).allocated.unsafeRunSync()._1
    val sendGridClient = SendGridClient(sendGridConfig, httpClient)
    val api: HttpRoutes[IO] = Main.api(sendGridClient)

    val response = api.orNotFound.run(Request(method = Method.GET, uri = uri"/tweets/article"))
    check[Seq[ArticleTweet]](response, Status.Ok, Some(List()))
  }

  def check[A](actual: IO[Response[IO]], expectedStatus: Status, expectedBody: Option[A])(implicit
      ev: EntityDecoder[IO, A]
  ): Unit = {
    val actualResp = actual.unsafeRunSync()
    actualResp.status shouldBe expectedStatus
    expectedBody.foreach(expected => actualResp.as[A].unsafeRunSync() shouldBe expected)
  }

}
