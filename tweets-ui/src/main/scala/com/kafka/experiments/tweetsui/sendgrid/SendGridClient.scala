package com.kafka.experiments.tweetsui.sendgrid

import cats.effect.{ConcurrentEffect, IO}
import com.kafka.experiments.tweetsui.config.SendGridConfig
import com.typesafe.scalalogging.StrictLogging
import io.circe.syntax.EncoderOps
import org.http4s.{AuthScheme, Credentials, MediaType, Method, Request}
import org.http4s.client.Client
import org.http4s.client.dsl.io._
import org.http4s.Method.{POST, PUT}
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.{Accept, Authorization}
import org.http4s.implicits.http4sLiteralsSyntax

import java.util.UUID

abstract class SendGridClient {
  def createSingleSend(html: String): IO[UUID]

  def sendSingleSendNow(singleSendId: UUID): IO[String]
}

object SendGridClient {

  def apply(config: SendGridConfig, httpClient: Client[IO]): DefaultSendGridClient = {
    new DefaultSendGridClient(config, httpClient)
  }
}

class DefaultSendGridClient(config: SendGridConfig, httpClient: Client[IO])
    extends SendGridClient
    with Http4sClientDsl[IO]
    with StrictLogging {

  val singleSendIdField = "id"

  val singleSendName = "Kafka Weekly Topics"
  val subject = "Kafka Weekly Topics"
  val singleSendUri = uri"https://api.sendgrid.com/v3/marketing/singlesends"

  override def createSingleSend(html: String): IO[UUID] = {
    import org.http4s.circe._

    val sendTo = SendTo(config.listIds)
    val emailConfig = EmailConfig(html, config.senderId, subject, config.unsubscribeListId)
    val singleSend = SingleSend(emailConfig, singleSendName, sendTo)

    val request = POST(
      singleSend.asJson,
      singleSendUri,
      Authorization(Credentials.Token(AuthScheme.Bearer, config.apiKey)),
      Accept(MediaType.application.json)
    )

    logger.info(s"Creating new single send with request: $request")
    httpClient
      .expect[String](request)
      .map(response => extractSingleSendId(response))
  }

  private def extractSingleSendId(response: String) = {
    import io.circe.parser._
    parse(response) match {
      case Left(parsingError) =>
        throw new RuntimeException("Unable to parse SingleSend creation response", parsingError)
      case Right(json) =>
        json.hcursor.downField(singleSendIdField).as[String].map(UUID.fromString) match {
          case Left(idParsingError) =>
            throw new RuntimeException("Unable to extract SingleSend UUID", idParsingError)
          case Right(id) =>
            logger.info(s"New single send created with id [$id]")
            id
        }
    }
  }

  override def sendSingleSendNow(singleSendId: UUID): IO[String] = {
    val scheduleUri = singleSendUri / singleSendId.toString / "schedule"
    val request = PUT(
      "{\"send_at\":\"now\"}",
      scheduleUri,
      Authorization(Credentials.Token(AuthScheme.Bearer, config.apiKey)),
      Accept(MediaType.application.json)
    )

    logger.info(s"Scheduling single send [$singleSendId] with request: $request")
    httpClient
      .expect[String](request)
  }
}
