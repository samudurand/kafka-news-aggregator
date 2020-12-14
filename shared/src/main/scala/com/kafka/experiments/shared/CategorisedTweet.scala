package com.kafka.experiments.shared

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

abstract class CategorisedTweet(val id: String, val text: String, val user: String, val createdAt: String)

object ExcludedTweet {
  implicit val codec: Codec[ExcludedTweet] = deriveCodec
}

case class ExcludedTweet(
    override val id: String,
    reason: String,
    override val text: String,
    override val user: String,
    override val createdAt: String
) extends CategorisedTweet(id, text, user, createdAt)

object AudioTweet {
  implicit val codec: Codec[AudioTweet] = deriveCodec
}

case class AudioTweet(
    override val id: String,
    override val text: String,
    override val user: String,
    override val createdAt: String
) extends CategorisedTweet(id, text, user, createdAt)

object VideoTweet {
  implicit val codec: Codec[VideoTweet] = deriveCodec
}

case class VideoTweet(
    override val id: String,
    override val text: String,
    override val user: String,
    override val createdAt: String
) extends CategorisedTweet(id, text, user, createdAt)

object VersionReleaseTweet {
  implicit val codec: Codec[VersionReleaseTweet] = deriveCodec
}

case class VersionReleaseTweet(
    override val id: String,
    override val text: String,
    override val user: String,
    override val createdAt: String
) extends CategorisedTweet(id, text, user, createdAt)

object ArticleTweet {
  implicit val codec: Codec[ArticleTweet] = deriveCodec
}

case class ArticleTweet(
    override val id: String,
    override val text: String,
    override val user: String,
    override val createdAt: String
) extends CategorisedTweet(id, text, user, createdAt)

object InterestingTweet {
  implicit val codec: Codec[InterestingTweet] = deriveCodec
}

case class InterestingTweet(
    override val id: String,
    override val text: String,
    override val user: String,
    override val createdAt: String
) extends CategorisedTweet(id, text, user, createdAt)
