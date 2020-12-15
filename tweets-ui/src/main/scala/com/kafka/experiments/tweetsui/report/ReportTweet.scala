package com.kafka.experiments.tweetsui.report

import com.kafka.experiments.shared.{CategorisedTweet, InterestingTweet}

object ReportTweet {
  def apply(categorisedTweet: InterestingTweet): ReportTweet =
    ReportTweet(categorisedTweet.user, categorisedTweet.text, categorisedTweet.url)
}

case class ReportTweet (user: String, text: String, url: String)
