package com.kafka.experiments.tweetscategorizer.ignore

import com.kafka.experiments.shared.{LinkUtils, Tweet}
import com.kafka.experiments.tweetscategorizer.KnownSources.hasSourceToBeExcluded
import com.kafka.experiments.tweetscategorizer.config.Keywords
import com.kafka.experiments.tweetscategorizer.utils.TextUtils.{textContainAtLeastOneNumber, textLoweredCaseContainAnyOf}
import com.kafka.experiments.tweetscategorizer.ignore.FranzKafkaWriter.isAboutFranzKafka
import com.kafka.experiments.tweetscategorizer.utils.UserUtils.userNameContainsAnyOf
import com.kafka.experiments.tweetscategorizer.utils.UserUtils

object ToExclude {

  val reasonDoesNotMentionKafka = "NO_KAFKA_MENTION"
  val reasonIsAboutAGame = "IS_ABOUT_A_GAME"
  val reasonIsAboutACertification = "IS_ABOUT_A_CERTIFICATION"
  val reasonIsAboutFranzKafka = "IS_ABOUT_F_KAFKA"
  val reasonIsAnAd = "IS_AN_AD"
  val reasonIsAJobOffer = "IS_A_JOB_OFFER"
  val reasonIsDiscountRelated = "IS_DISCOUNT_RELATED"
  val reasonIsMoneyRelated = "IS_MONEY_RELATED"
  val reasonIsTooShort = "IS_TOO_SHORT"
  val reasonHasSourceToBeExcluded = "HAS_SOURCE_TO_BE_EXCLUDED"
  val reasonHasUnrelatedWords = "HAS_UNRELATED_WORDS"
  val reasonHasExcludedTags = "HAS_EXCLUDED_TAGS"

  /** @return the reason why it should be excluded
    */
  def shouldBeExcluded(tweet: Tweet): Option[String] = {
    tweet match {
      case t if doesNotMentionKafka(t)   => Some(reasonDoesNotMentionKafka)
      case t if hasSourceToBeExcluded(t) => Some(reasonHasSourceToBeExcluded)
      case t if hasSourceToBeExcluded(t) => Some(reasonHasSourceToBeExcluded)
      case t if hasUnrelatedContent(t)   => Some(reasonHasUnrelatedWords)
      case t if isAboutACertification(t) => Some(reasonIsAboutACertification)
      case t if isAboutAGame(t)          => Some(reasonIsAboutAGame)
      case t if isAboutFranzKafka(t)     => Some(reasonIsAboutFranzKafka)
      case t if isAnAdvertisement(t)     => Some(reasonIsAnAd)
      case t if isAJobOffer(t)           => Some(reasonIsAJobOffer)
      case t if isDiscountRelated(t)     => Some(reasonIsDiscountRelated)
      case t if isMoneyRelated(t)        => Some(reasonIsMoneyRelated)
      case t if isTooShort(t)            => Some(reasonIsTooShort)
      case t if hasTagToExclude(t)       => Some(reasonHasExcludedTags)
      case _                             => None
    }
  }

  private def isAboutAGame(tweet: Tweet): Boolean = {
    textLoweredCaseContainAnyOf(tweet.Text, List(), Keywords.gameWords) ||
    userNameContainsAnyOf(tweet.User, Keywords.gameWords)
  }

  private def doesNotMentionKafka(tweet: Tweet): Boolean = {
    !(textLoweredCaseContainAnyOf(tweet.Text, List("kafka")) ||
      LinkUtils.firstValidLink(tweet.URLEntities).exists(_.ExpandedURL.contains("kafka")))
  }

  private def isTooShort(tweet: Tweet) = {
    val minTextLenght = 10
    tweet.Text.length < minTextLenght
  }

  private def isAJobOffer(tweet: Tweet) = {
    textLoweredCaseContainAnyOf(tweet.Text, Keywords.jobOfferWords)
  }

  private def isDiscountRelated(tweet: Tweet) = {
    textLoweredCaseContainAnyOf(tweet.Text, Keywords.discountWords)
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
    textLoweredCaseContainAnyOf(tweet.Text, List(), Keywords.certification)
  }

  private def isAnAdvertisement(tweet: Tweet) = {
    textLoweredCaseContainAnyOf(tweet.Text, Keywords.adWords)
  }

  private def hasUnrelatedContent(tweet: Tweet): Boolean = {
    textLoweredCaseContainAnyOf(tweet.Text, Keywords.unrelatedWords) ||
    Keywords.unrelatedDomains.exists(domain => tweet.URLEntities.exists(_.ExpandedURL.contains(domain)))
  }

  private def hasTagToExclude(tweet: Tweet): Boolean = {
    Keywords.tagsToExclude
      .map(_.toLowerCase)
      .exists(tweet.UserMentionEntities.map(_.ScreenName.toLowerCase).contains(_))
  }
}
