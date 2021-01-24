package com.kafka.experiments.tweetsui.newsletter

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

object NewsletterTweetDraft {
  implicit val codec: Codec[NewsletterTweetDraft] = deriveCodec
}

case class NewsletterTweetDraft(
    id: String,
    user: String,
    text: String,
    url: String,
    createdAt: String
)

object NewsletterTweet {
  implicit val codec: Codec[NewsletterTweet] = deriveCodec

  def apply(category: String, tweet: NewsletterTweetDraft): NewsletterTweet =
    NewsletterTweet(tweet.id, tweet.user, tweet.text, tweet.url, tweet.createdAt, category)
}

case class NewsletterTweet(
    id: String,
    user: String,
    text: String,
    url: String,
    createdAt: String,
    category: String,
    score: Long = -1,
    favourite: Boolean = false
)
