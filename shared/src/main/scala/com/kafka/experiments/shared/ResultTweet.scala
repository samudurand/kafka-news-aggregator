package com.kafka.experiments.shared

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

trait Tweet

object DroppedTweet {
  val typeName = "dropped"
  implicit val codec: Codec[DroppedTweet] = deriveCodec
}

case class DroppedTweet (
    text: String,
    retweet: Boolean,
    reason: String,
    user: String
)  extends Tweet

object InterestingTweet {
  val typeName = "interesting"
  implicit val codec: Codec[InterestingTweet] = deriveCodec
}

case class InterestingTweet(
    text: String,
    retweet: Boolean,
    user: String
) extends Tweet