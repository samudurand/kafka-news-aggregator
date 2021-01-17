package com.kafka.experiments.tweetsui.client

import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import org.http4s.Method.GET
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl

trait MediumClient {
  def retrieveClapCount(articleUrl: String): IO[Option[Long]]
}

object MediumClient {
  def apply(httpClient: Client[IO]) = new DefaultMediumClient(httpClient)
}

class DefaultMediumClient(httpClient: Client[IO]) extends MediumClient with Http4sClientDsl[IO] with StrictLogging {

  override def retrieveClapCount(articleUrl: String): IO[Option[Long]] = {
        val request = GET(Uri.unsafeFromString(articleUrl))

        logger.info(s"New Medium query with request: $request")

        httpClient
          .expect[String](request)
          .map(response => extractClapCount(response, articleUrl))
  }

  private def extractClapCount(htmlBody: String, url: String): Option[Long] = {
    val clapCountPattern = "\"clapCount\":[0-9]+,".r
    val countPattern = "[0-9]+".r

    try {
      clapCountPattern.findFirstIn(htmlBody)
        .flatMap(a => countPattern.findFirstIn(a))
        .map(_.toLong)
    } catch {
      case ex: Throwable =>
        logger.error(s"Unable to extract clap count from $url", ex)
        None
    }
  }
}
