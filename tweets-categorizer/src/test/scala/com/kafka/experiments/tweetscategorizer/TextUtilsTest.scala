package com.kafka.experiments.tweetscategorizer

import com.kafka.experiments.tweetscategorizer.utils.TextUtils.textContainAnyOf
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TextUtilsTest extends AnyFlatSpec with Matchers {

  "Text not containing the searched substrings" should "not match" in {
    textContainAnyOf("the picking", List("stick")) shouldBe false
  }

  "Text containing at least one of the searched substrings" should "match" in {
    textContainAnyOf("sticking out", List("stick", "pick")) shouldBe true
  }

  "Text containing at least one of the searched words" should "match" in {
    textContainAnyOf("stick out", List(), List("stick", "pick")) shouldBe true
  }

  "Text containing at least one of the searched words with punctuation" should "match" in {
    textContainAnyOf("stick! out", List(), List("stick", "pick")) shouldBe true
  }

  "Text containing one of the searched substrings or words" should "match" in {
    textContainAnyOf("stick out", List("lick"), List("stick", "pick")) shouldBe true
  }
}
