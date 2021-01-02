package com.kafka.experiments.tweetsui

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

trait MockYoutubeApi extends BeforeAndAfterEach {
  this: AnyFlatSpec with Matchers =>

  protected val youtubeApi = new WireMockServer(wireMockConfig().port(4001))
  protected val mockYoutubeUrl = "http://localhost:4001"

  override def beforeEach: Unit = {
    super.beforeEach()
    youtubeApi.resetAll()
    youtubeApi.start()
  }

  override def afterEach: Unit = {
    super.afterEach()
    youtubeApi.stop()
  }
}
