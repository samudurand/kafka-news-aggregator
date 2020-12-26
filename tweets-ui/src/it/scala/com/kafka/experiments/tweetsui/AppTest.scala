package com.kafka.experiments.tweetsui

import com.dimafeng.testcontainers.{ForEachTestContainer, MongoDBContainer}
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, urlPathEqualTo}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec

class AppTest extends AnyFlatSpec with ForEachTestContainer with BeforeAndAfterEach {

  override val container: MongoDBContainer = MongoDBContainer()
  private val wireMockServer = new WireMockServer(wireMockConfig().port(4000))

  override def beforeEach: Unit = {
    wireMockServer.start()
  }

  override def afterEach: Unit = {
    wireMockServer.stop()
  }

  "API" should "retrieve tweets in category Article" in {
    wireMockServer.stubFor(
      get(urlPathEqualTo("/v3/marketing/singlesends"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
        )
    )


  }

}
