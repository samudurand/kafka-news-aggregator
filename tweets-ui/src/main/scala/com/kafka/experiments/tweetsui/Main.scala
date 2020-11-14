package com.kafka.experiments.tweetsui

import com.kafka.experiments.tweetsui.config.GlobalConfig
import com.typesafe.scalalogging.StrictLogging
import pureconfig.ConfigSource
import cats.effect.{ExitCode, IO, IOApp}
import com.kafka.experiments.shared.{DroppedTweet, InterestingTweet}
import org.http4s.HttpRoutes
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.dsl.io._
import org.http4s.server.blaze.BlazeServerBuilder
import pureconfig.generic.auto.exportReader
import Encoders._

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends IOApp with StrictLogging {

  private val config = ConfigSource.default.loadOrThrow[GlobalConfig]

  private val mongoService = MongoService.apply(config.mongodb)

  private val helloWorldService = HttpRoutes
    .of[IO] {
      case GET -> Root / tweetType =>
        tweetType match {
          case InterestingTweet.typeName =>
            mongoService.interestingTweets().flatMap(Ok(_))
          case DroppedTweet.typeName =>
            mongoService.droppedTweets().flatMap(Ok(_))
          case _ => BadRequest()
        }

      case GET -> Root / tweetType / "count" =>
        tweetType match {
          case InterestingTweet.typeName =>
            mongoService.interestingTweetsCount().flatMap(Ok(_))
          case DroppedTweet.typeName =>
            mongoService.droppedTweetsCount().flatMap(Ok(_))
          case _ => BadRequest()
        }
    }
    .orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](global)
      .bindHttp(config.server.port)
      .withHttpApp(helloWorldService)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
