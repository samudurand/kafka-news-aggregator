package com.kafka.experiments.shared

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class UrlManipulatorTest extends AnyFlatSpec with Matchers {

  "Urls" should "be removed" in {
    UrlManipulator.removeUrls("Links: https://some.com/link") shouldBe "Links: "
  }

}
