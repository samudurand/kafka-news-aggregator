package com.kafka.experiments.tweetsui.newsletter

import com.kafka.experiments.tweetsui.Other
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

object NewsletterTweet {
  implicit val codec: Codec[NewsletterTweet] = deriveCodec
}

case class NewsletterTweet(
    id: String,
    user: String,
    text: String,
    url: String,
    createdAt: String
)

object CompleteNewsletterTweet {
  implicit val codec: Codec[CompleteNewsletterTweet] = deriveCodec

  def apply(category: String, tweet: NewsletterTweet): CompleteNewsletterTweet =
    CompleteNewsletterTweet(tweet.id, tweet.user, tweet.text, tweet.url, tweet.createdAt, category)
}

case class CompleteNewsletterTweet(
    id: String,
    user: String,
    text: String,
    url: String,
    createdAt: String,
    category: String
)
