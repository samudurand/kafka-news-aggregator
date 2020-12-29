package com.kafka.experiments.tweetsui

import cats.effect.IO
import com.kafka.experiments.tweetsui.newsletter.NewsletterTweet

class ScoringService {

  def calculateScores(tweets: Seq[NewsletterTweet]): IO[Unit] = {
    IO.pure(())
  }

}
