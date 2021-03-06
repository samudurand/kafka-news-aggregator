package com.kafka.experiments.tweetscategorizer.utils

object TextUtils {

  def textLoweredCaseContainAnyOf(
      text: String,
      substringWords: Seq[String],
      fullWords: Seq[String] = List()
  ): Boolean = {
    val wordSeparatorOrPunctuation = "[\\s+!?#.]"
    substringWords.exists(text.toLowerCase.contains) ||
    text.toLowerCase.split(wordSeparatorOrPunctuation).exists(fullWords.contains)
  }

  /** @param substringWords Words matched as substrings in the text
    * @param fullWords Words that have to be matched exactly
    */
  def textContainAnyOf(text: String, substringWords: List[String], fullWords: List[String] = List()): Boolean = {
    val wordSeparatorOrPunctuation = "[\\s+!?#.]"
    substringWords.exists(text.contains) ||
    text.split(wordSeparatorOrPunctuation).exists(fullWords.contains)
  }

  def textContainAtLeastOneNumber(text: String): Boolean = {
    text.exists(_.isDigit)
  }

}
