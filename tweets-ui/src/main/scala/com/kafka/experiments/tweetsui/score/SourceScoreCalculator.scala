package com.kafka.experiments.tweetsui.score

import cats.effect.IO
import com.kafka.experiments.tweetsui.config.SourceConfig
import com.kafka.experiments.tweetsui.newsletter.NewsletterTweet

trait SourceScoreCalculator extends ScoreCalculator

object SourceScoreCalculator {
  def apply(config: SourceConfig) = new DefaultSourceScoreCalculator(config)
}

class DefaultSourceScoreCalculator(config: SourceConfig) extends SourceScoreCalculator {

  private val poorSourceScore = 1000
  private val poorSourceFactor = -2

  override def calculate(tweets: Seq[NewsletterTweet]): IO[Map[String, Seq[Score]]] = {
    IO {
      tweets.map(tweet =>
        if (config.poor.exists(source => source.toLowerCase == tweet.user.toLowerCase)) {
          tweet.id -> List(Score(s"Poor Source [${tweet.user}]", poorSourceScore, poorSourceFactor))
        } else {
          tweet.id -> List()
        }
      ).toMap
    }
  }
}
