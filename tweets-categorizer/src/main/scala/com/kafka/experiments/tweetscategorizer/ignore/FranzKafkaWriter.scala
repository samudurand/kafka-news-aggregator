package com.kafka.experiments.tweetscategorizer.ignore

import com.kafka.experiments.shared.Tweet
import com.kafka.experiments.tweetscategorizer.config.Keywords

object FranzKafkaWriter {

  private val bookNames = List(
    "Amerika",
    "Before the Law",
    "Excursion into the Mountains",
    "Kafka on the Shore", // Not by F. Kafka but another popular book
    "Letters to Felice",
    "Metamorphosis",
    "The Castle",
    "The Trial"
  ).map(_.toLowerCase)

  private val authorRelatedWords = Keywords.franzKafkaRelatedWords.map(_.toLowerCase)

  private val kafkaUserAccount = "Kafka"

  def isAboutFranzKafka(tweet: Tweet): Boolean = {
    containsBookName(tweet.Text) ||
    containsAuthorName(tweet.Text) ||
    containsAWordRelatedToFranzKafka(tweet.Text) ||
    mentionsAuthorAccount(tweet)
  }

  private def containsBookName(text: String): Boolean = {
    val lowerCasedText = text.toLowerCase
    bookNames.exists(lowerCasedText.contains)
  }

  private def containsAuthorName(text: String): Boolean = {
    text.toLowerCase.contains("franz") // Rare enough name to not be an issue
  }

  private def containsAWordRelatedToFranzKafka(text: String): Boolean = {
    val lowerCasedText = text.toLowerCase
    authorRelatedWords.map(_.toLowerCase).exists(lowerCasedText.contains)
  }

  private def mentionsAuthorAccount(tweet: Tweet): Boolean = {
    tweet.UserMentionEntities.exists(user => user.ScreenName.equalsIgnoreCase(kafkaUserAccount))
  }

}
