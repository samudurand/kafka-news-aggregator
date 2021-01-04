package com.kafka.experiments.tweetscategorizer

import com.kafka.experiments.shared.UrlManipulator
import com.kafka.experiments.tweetscategorizer.utils.LinkUtils.{containsValidLink, extractBaseUrl}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LinkUtilsTest extends AnyFlatSpec with Matchers {

  "Tweet with a link" should "be identified" in {
    containsValidLink(List(URLEntity("https://t.co/0lztrRpQTK", "http://google.com"))) shouldBe true
  }

  "Tweet without a link" should "be identified" in {
    containsValidLink(List()) shouldBe false
  }

  "Tweet with only a twitter link" should "be identified" in {
    containsValidLink(List(URLEntity("https://t.co/0lztrRpQTK", "https://twitter.com/some/tweet"))) shouldBe false
  }

  "Url" should "be returned without parameters" in {
    extractBaseUrl(
      "https://www.infoq.com/articles"
    ) shouldBe "https://www.infoq.com/articles"

    extractBaseUrl(
      "https://www.infoq.com/articles/?utm_campaign=infoq_content&utm_source=dlvr.it"
    ) shouldBe "https://www.infoq.com/articles/"
  }

  // For manually expanding URL
  ignore should "expand url" in {
    UrlManipulator.expandUrlOnce("https://t.co/bspmxsKVEP") shouldBe ""
  }

}
