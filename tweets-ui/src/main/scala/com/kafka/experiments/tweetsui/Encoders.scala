package com.kafka.experiments.tweetsui

import cats.effect.IO
import com.kafka.experiments.shared._
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

object Encoders {
  implicit val intEncoder: EntityEncoder[IO, Long] = jsonEncoderOf[IO, Long]
  implicit val interestingEncoder: EntityEncoder[IO, Seq[InterestingTweet]] = jsonEncoderOf[IO, Seq[InterestingTweet]]
  implicit val audioEncoder: EntityEncoder[IO, Seq[AudioTweet]] = jsonEncoderOf[IO, Seq[AudioTweet]]
  implicit val videoEncoder: EntityEncoder[IO, Seq[VideoTweet]] = jsonEncoderOf[IO, Seq[VideoTweet]]
  implicit val articleEncoder: EntityEncoder[IO, Seq[ArticleTweet]] = jsonEncoderOf[IO, Seq[ArticleTweet]]
  implicit val versionEncoder: EntityEncoder[IO, Seq[VersionReleaseTweet]] = jsonEncoderOf[IO, Seq[VersionReleaseTweet]]
  implicit val excludedEncoder: EntityEncoder[IO, Seq[ExcludedTweet]] = jsonEncoderOf[IO, Seq[ExcludedTweet]]
  implicit val countResultEncoder: EntityEncoder[IO, CountResult] = jsonEncoderOf[IO, CountResult]
}
