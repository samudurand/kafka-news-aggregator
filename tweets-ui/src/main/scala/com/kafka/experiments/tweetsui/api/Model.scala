package com.kafka.experiments.tweetsui.api

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

object CountResult {
  implicit val codec: Codec[CountResult] = deriveCodec
}
case class CountResult(count: Long)

object MoveTweetsToNewsletter {
  implicit val codec: Codec[MoveTweetsToNewsletter] = deriveCodec
}
case class MoveTweetsToNewsletter(tweetIds: Map[String, List[String]])

object UpdateNewsletterTweet {
  implicit val codec: Codec[UpdateNewsletterTweet] = deriveCodec
}
case class UpdateNewsletterTweet(tweetId: String, category: Option[String], favourite: Option[Boolean])

object UpdateTweet {
  implicit val codec: Codec[UpdateTweet] = deriveCodec
}
case class UpdateTweet(favourite: Boolean)