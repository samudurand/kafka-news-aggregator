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

