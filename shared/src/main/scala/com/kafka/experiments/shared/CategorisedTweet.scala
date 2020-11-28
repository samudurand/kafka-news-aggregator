package com.kafka.experiments.shared

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

trait CategorisedTweet

object ExcludedTweet {
  val typeName = "excluded"
  implicit val codec: Codec[ExcludedTweet] = deriveCodec
}

case class ExcludedTweet(
    id: String,
    reason: String,
    text: String,
    user: String,
    createdAt: String
) extends CategorisedTweet

object AudioTweet {
  val typeName = "audio"
  implicit val codec: Codec[AudioTweet] = deriveCodec
}

case class AudioTweet(
    id: String,
    text: String,
    user: String,
    createdAt: String
) extends CategorisedTweet

object VideoTweet {
  val typeName = "video"
  implicit val codec: Codec[VideoTweet] = deriveCodec
}

case class VideoTweet(
    id: String,
    text: String,
    user: String,
    createdAt: String
) extends CategorisedTweet

object VersionReleaseTweet {
  val typeName = "version"
  implicit val codec: Codec[VersionReleaseTweet] = deriveCodec
}

case class VersionReleaseTweet(
    id: String,
    text: String,
    user: String,
    createdAt: String
) extends CategorisedTweet

object ArticleTweet {
  val typeName = "article"
  implicit val codec: Codec[ArticleTweet] = deriveCodec
}

case class ArticleTweet(
    id: String,
    text: String,
    user: String,
    createdAt: String
) extends CategorisedTweet

object InterestingTweet {
  val typeName = "interesting"
  implicit val codec: Codec[InterestingTweet] = deriveCodec
}

case class InterestingTweet(
    id: String,
    text: String,
    user: String,
    createdAt: String
) extends CategorisedTweet
