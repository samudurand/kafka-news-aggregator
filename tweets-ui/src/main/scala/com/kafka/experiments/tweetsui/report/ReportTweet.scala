package com.kafka.experiments.tweetsui.report

import com.kafka.experiments.shared.CategorisedTweet

object ReportTweet {
  def apply(categorisedTweet: CategorisedTweet): ReportTweet = ReportTweet(categorisedTweet.text)
}

case class ReportTweet (text: String)
