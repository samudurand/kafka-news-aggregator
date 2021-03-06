package com.kafka.experiments.tweetsui

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

trait MockSendGrid extends BeforeAndAfterEach with Matchers {
  this: AnyFlatSpec =>

  protected val sendgridApi = new WireMockServer(wireMockConfig().port(4000))
  protected val mockSendGridUrl = "http://localhost:4000"

  override def beforeEach: Unit = {
    super.beforeEach()
    sendgridApi.resetAll()
    sendgridApi.start()
  }

  override def afterEach: Unit = {
    super.afterEach()
    sendgridApi.stop()
  }

  def verifyZeroInteractionsWithSendGridServer(): Unit = {
    sendgridApi.getServeEvents.getRequests.size() shouldBe 0
  }
}
