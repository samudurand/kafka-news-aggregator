package com.kafka.experiments.tweetsui

import cats.effect.IO
import com.kafka.experiments.shared.{DroppedTweet, InterestingTweet}
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

object Encoders {
  implicit val intEncoder: EntityEncoder[IO, Long] = jsonEncoderOf[IO, Long]
  implicit val interestingEncoder: EntityEncoder[IO, Seq[InterestingTweet]] = jsonEncoderOf[IO, Seq[InterestingTweet]]
  implicit val droppedEncoder: EntityEncoder[IO, Seq[DroppedTweet]] = jsonEncoderOf[IO, Seq[DroppedTweet]]
}
