package com.kafka.experiments.tweetsui.score

import com.kafka.experiments.tweetsui.config.SourceConfig
import com.kafka.experiments.tweetsui.newsletter.NewsletterTweet
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DefaultSourceScoreCalculatorTest extends AnyFlatSpec with Matchers {

  private val calculator = SourceScoreCalculator(SourceConfig(List("badsource", "otherbadsource")))

  "Calculator" should "apply a bad score only to poor sources" in {
    val scores = calculator.calculate(List(
      NewsletterTweet("1", "badsource", "some tweet", "http://google.com", "123456789", "article"),
      NewsletterTweet("2", "goodsource", "some other tweet", "http://google.com", "123456789", "article")
    ))

    scores.unsafeRunSync()("1") shouldBe List(Score("Poor Source [badsource]", 1000, -1))
    scores.unsafeRunSync()("2") shouldBe List()
  }

}
