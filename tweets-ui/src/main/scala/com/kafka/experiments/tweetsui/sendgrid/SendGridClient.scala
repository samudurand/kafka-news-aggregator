package com.kafka.experiments.tweetsui.sendgrid

import cats.effect.{ConcurrentEffect, IO}
import com.kafka.experiments.tweetsui.config.SendGridConfig
import io.circe.syntax.EncoderOps
import org.http4s.{AuthScheme, Credentials, MediaType, Method, Request}
import org.http4s.client.Client
import org.http4s.client.dsl.io._
import org.http4s.Method.POST
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.{Accept, Authorization}
import org.http4s.implicits.http4sLiteralsSyntax

abstract class SendGridClient[F[_]: ConcurrentEffect] {
  def createSingleSend(html: String): F[String]

//  def sendSingleSendNow(): F[String]
}

object SendGridClient {

  def apply[F[_]: ConcurrentEffect](config: SendGridConfig, httpClient: Client[F]): DefaultSendGridClient[F] = {
    new DefaultSendGridClient(config, httpClient)
  }
}

class DefaultSendGridClient[F[_]: ConcurrentEffect](config: SendGridConfig, httpClient: Client[F])
  extends SendGridClient with Http4sClientDsl[F] {

  val singleSendName = "Kafka Weekly Topics"
  val subject = "Kafka Weekly Topics"
  val singleSendUri = uri"https://api.sendgrid.com/v3/marketing/singlesends"

  override def createSingleSend(html: String): F[String] = {
    import Encoders._
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

    httpClient.expect[String](request)
  }

//  override def sendSingleSendNow(): F[String] = {
//    IO.pure("")
//  }
}
