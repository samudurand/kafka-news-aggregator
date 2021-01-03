package com.kafka.experiments.shared

import com.linkedin.urls.detection.{UrlDetector, UrlDetectorOptions}
import com.typesafe.scalalogging.StrictLogging
import jdk.internal.util.Preconditions
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.HttpHead
import org.apache.http.impl.client.HttpClientBuilder

import java.net.{HttpURLConnection, MalformedURLException, URL}
import scala.jdk.CollectionConverters._

object UrlManipulator extends StrictLogging {

  def removeUrls(text: String): String = {
    val urls = new UrlDetector(text, UrlDetectorOptions.Default).detect()
    val cleanedText = urls.asScala.foldLeft(text)((text, url) => text.replace(url.getOriginalUrl, ""))
    cleanedText
  }

  def expandUrlOnce(url: String): String = {
    val client = HttpClientBuilder.create().disableRedirectHandling().build();
    val request = new HttpHead(url)
    try {
      val httpResponse = client.execute(request)
      val statusCode = httpResponse.getStatusLine.getStatusCode
      if (statusCode != 301 && statusCode != 302) {
        url
      } else {
        val headers = httpResponse.getHeaders(HttpHeaders.LOCATION)
        headers(0).getValue
      }
    } catch {
      case ex: Throwable =>
        logger.error(s"Unable to expand URL [$url]", ex)
        url
    } finally {
      if (request != null) {
        request.releaseConnection()
      }
    }
  }
}
