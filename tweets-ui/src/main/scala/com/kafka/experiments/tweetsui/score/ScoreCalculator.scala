package com.kafka.experiments.tweetsui.score

import cats.effect.IO
import com.kafka.experiments.tweetsui.config.ScaledScoreConfig
import com.kafka.experiments.tweetsui.newsletter.NewsletterTweet

/** @param name: used for debugging purpose
  * @param value: the score value on a scale of 0 to 1000
  * @param factor: the factor defining how much impact that score should have on the total score (usually 1 or 2)
  */
case class Score(name: String, value: Int, factor: Int)

trait ScoreCalculator {
  def calculate(tweets: Seq[NewsletterTweet]): IO[Map[String, Seq[Score]]]

  protected def calculateScaledScore(name: String, config: ScaledScoreConfig, value: Long): Score = {
    val score = calculateCountScore(config.getScale, value)
    Score(name, score, config.factor)
  }

  protected def calculateCountScore(scale: Map[Int, Int], count: Long): Int = {
    scale(determineScaleRange(scale, count))
  }

  protected def determineScaleRange(scale: Map[Int, Int], value: Long): Int = {
    scale.keys.toList.sorted.reverse.find(_ <= value) match {
      case Some(rank) => rank
      case None       => throw new RuntimeException(s"Unable to find a rank on scale for value $value")
    }
  }
}
