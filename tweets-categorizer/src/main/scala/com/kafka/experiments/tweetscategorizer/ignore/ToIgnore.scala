package com.kafka.experiments.tweetscategorizer.ignore

import com.kafka.experiments.tweetscategorizer.KnownSources.hasSourceToBeIgnored
import com.kafka.experiments.tweetscategorizer.TextUtils.textLoweredCaseContainAnyOf
import com.kafka.experiments.tweetscategorizer.{Keywords, Tweet}
import com.kafka.experiments.tweetscategorizer.ignore.FranzKafkaWriter.isAboutFranzKafka

object ToIgnore {

  val reasonDoesNotMentionKafka = "NO_KAFKA_MENTION"
  val reasonIsAboutAGame = "IS_ABOUT_A_GAME"
  val reasonIsAboutACertification = "IS_ABOUT_A_CERTIFICATION"
  val reasonIsAboutFranzKafka = "IS_ABOUT_F_KAFKA"
  val reasonIsAnAd = "IS_AN_AD"
  val reasonIsAJobOffer = "IS_A_JOB_OFFER"
  val reasonIsMoneyRelated = "IS_MONEY_RELATED"
  val reasonIsNotInEnglish = "IS_NOT_IN_ENGLISH"
  val reasonIsTooShort = "IS_TOO_SHORT"
  val reasonHasSourceToBeIgnored = "HAS_SOURCE_TO_BE_IGNORED"
  val reasonHasUnrelatedWords = "HAS_UNRELATED_WORDS"

  /**
   * @return the reason why it should be ignored
   */
  def shouldBeIgnored(tweet: Tweet): Option[String] = {
    tweet match {
      case t if doesNotMentionKafka(t) => Some(reasonDoesNotMentionKafka)
      case t if hasSourceToBeIgnored(t) => Some(reasonHasSourceToBeIgnored)
      case t if hasUnrelatedWords(t) => Some(reasonHasUnrelatedWords)
      case t if isAboutACertification(t) => Some(reasonIsAboutACertification)
      case t if isAboutAGame(t) => Some(reasonIsAboutAGame)
      case t if isAboutFranzKafka(t) => Some(reasonIsAboutFranzKafka)
      case t if isAnAdvertisement(t) => Some(reasonIsAnAd)
      case t if isAJobOffer(t) => Some(reasonIsAJobOffer)
      case t if isMoneyRelated(t) => Some(reasonIsMoneyRelated)
      case t if isNotInEnglish(t) => Some(reasonIsNotInEnglish)
      case t if isTooShort(t) => Some(reasonIsTooShort)
      case _ => None
    }
  }

  private def isAboutAGame(tweet: Tweet): Boolean = {
    val gameWords = List("game", "indie")
    textLoweredCaseContainAnyOf(tweet.Text, List(), gameWords)
  }

  private def doesNotMentionKafka(tweet: Tweet): Boolean = {
    !textLoweredCaseContainAnyOf(tweet.Text, List("kafka"))
  }

  private def isNotInEnglish(tweet: Tweet) = {
    !tweet.Lang.getOrElse("").toLowerCase.equals("en")
  }

  private def isTooShort(tweet: Tweet) = {
    val minTextLenght = 10
    tweet.Text.length < minTextLenght
  }

  private def isAJobOffer(tweet: Tweet) = {
    textLoweredCaseContainAnyOf(tweet.Text, Keywords.jobOfferWords)
  }

  private def isMoneyRelated(tweet: Tweet) = {
    val regexSignAfter = "[0-9]+[\\$£€]".r
    val regexSignBefore = "[\\$£€][0-9]+".r

    val saleWords = List("black friday")

    ((regexSignAfter.findFirstIn(tweet.Text), regexSignBefore.findFirstIn(tweet.Text)) match {
      case (None, None) => false
      case _            => true
    }) || textLoweredCaseContainAnyOf(tweet.Text, saleWords)
  }

  private def isAboutACertification(tweet: Tweet): Boolean = {
    val certificationsWords = List("certificate", "certification")
    textLoweredCaseContainAnyOf(tweet.Text, List(), certificationsWords)
  }

  private def isAnAdvertisement(tweet: Tweet) = {
    val adWords = List("sponsored")
    textLoweredCaseContainAnyOf(tweet.Text, adWords)
  }

  private def hasUnrelatedWords(tweet: Tweet) = {
    textLoweredCaseContainAnyOf(tweet.Text, Keywords.unrelatedWords)
  }
}
