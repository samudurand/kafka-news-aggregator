package com.kafka.experiments.tweetsui

import cats.effect.IO
import com.kafka.experiments.shared._
import com.kafka.experiments.tweetsui.newsletter.CompleteNewsletterTweet
import io.circe.Json
import org.http4s.{EntityDecoder, EntityEncoder, circe}
import org.http4s.circe.jsonEncoderOf

object Decoders {
  implicit val articleDecoder: EntityDecoder[IO, Seq[ArticleTweet]] = circe.jsonOf[IO, Seq[ArticleTweet]]
  implicit val audioDecoder: EntityDecoder[IO, Seq[AudioTweet]] = circe.jsonOf[IO, Seq[AudioTweet]]
  implicit val otherDecoder: EntityDecoder[IO, Seq[OtherTweet]] = circe.jsonOf[IO, Seq[OtherTweet]]
  implicit val versionDecoder: EntityDecoder[IO, Seq[VersionReleaseTweet]] = circe.jsonOf[IO, Seq[VersionReleaseTweet]]
  implicit val videoDecoder: EntityDecoder[IO, Seq[VideoTweet]] = circe.jsonOf[IO, Seq[VideoTweet]]
  implicit val excludedDecoder: EntityDecoder[IO, Seq[ExcludedTweet]] = circe.jsonOf[IO, Seq[ExcludedTweet]]
  implicit val countResultDecoder: EntityDecoder[IO, CountResult] = circe.jsonOf[IO, CountResult]

}

object Encoders {
  implicit val longEncoder: EntityEncoder[IO, Long] = jsonEncoderOf[IO, Long]
  implicit val interestingEncoder: EntityEncoder[IO, Seq[OtherTweet]] = jsonEncoderOf[IO, Seq[OtherTweet]]
  implicit val audioEncoder: EntityEncoder[IO, Seq[AudioTweet]] = jsonEncoderOf[IO, Seq[AudioTweet]]
  implicit val videoEncoder: EntityEncoder[IO, Seq[VideoTweet]] = jsonEncoderOf[IO, Seq[VideoTweet]]
  implicit val articleEncoder: EntityEncoder[IO, Seq[ArticleTweet]] = jsonEncoderOf[IO, Seq[ArticleTweet]]
  implicit val versionEncoder: EntityEncoder[IO, Seq[VersionReleaseTweet]] = jsonEncoderOf[IO, Seq[VersionReleaseTweet]]
  implicit val excludedEncoder: EntityEncoder[IO, Seq[ExcludedTweet]] = jsonEncoderOf[IO, Seq[ExcludedTweet]]
  implicit val countResultEncoder: EntityEncoder[IO, CountResult] = jsonEncoderOf[IO, CountResult]

  implicit val newsTweetEncoder: EntityEncoder[IO, Seq[CompleteNewsletterTweet]] =
    jsonEncoderOf[IO, Seq[CompleteNewsletterTweet]]
}
