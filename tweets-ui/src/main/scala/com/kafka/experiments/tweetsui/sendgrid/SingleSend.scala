package com.kafka.experiments.tweetsui.sendgrid

import cats.effect.IO
import io.circe.Codec
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import io.circe.generic.semiauto.deriveCodec

object SingleSend {
  implicit val codec: Codec[SingleSend] = deriveCodec
}
case class SingleSend(email_config: EmailConfig, name: String, send_to: SendTo)

object SendTo {
  implicit val codec: Codec[SendTo] = deriveCodec
}
case class SendTo(list_ids: List[String])

object EmailConfig {
  implicit val codec: Codec[EmailConfig] = deriveCodec
}
case class EmailConfig(html_content: String, sender_id: Int, subject: String, suppression_group_id: Int)

object Encoders {
  implicit val singleSendEncoder: EntityEncoder[IO, SingleSend] = jsonEncoderOf[IO, SingleSend]
  implicit val sendToEncoder: EntityEncoder[IO, SendTo] = jsonEncoderOf[IO, SendTo]
  implicit val emailConfigEncoder: EntityEncoder[IO, EmailConfig] = jsonEncoderOf[IO, EmailConfig]
}
