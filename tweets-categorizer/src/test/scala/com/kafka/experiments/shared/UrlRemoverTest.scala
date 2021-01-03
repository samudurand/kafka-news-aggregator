package com.kafka.experiments.shared

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class UrlRemoverTest extends AnyFlatSpec with Matchers {

  "Urls" should "be removed" in {
    UrlRemover.removeUrls("Links: https://some.com/link") shouldBe "Links: "
  }

}
