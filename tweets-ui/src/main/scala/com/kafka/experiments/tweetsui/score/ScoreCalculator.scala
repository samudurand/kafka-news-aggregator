package com.kafka.experiments.tweetsui.score

import cats.effect.IO
import com.kafka.experiments.tweetsui.newsletter.NewsletterTweet

/**
 * @param name: used for debugging purpose
 * @param value: the score value on a scale of 0 to 1000
 * @param factor: the factor defining how much impact that score should have on the total score (usually 1 or 2)
 */
case class Score(name: String, value: Int, factor: Int)

trait ScoreCalculator {
  def calculate(tweets: Seq[NewsletterTweet]): IO[Map[String, Seq[Score]]]
}
