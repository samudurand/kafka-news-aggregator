package com.kafka.experiments.shared

import com.linkedin.urls.detection.{UrlDetector, UrlDetectorOptions}
import scala.jdk.CollectionConverters._

object UrlRemover {
  def removeUrls(text: String) = {
    val urls = new UrlDetector(text, UrlDetectorOptions.Default).detect()
    val cleanedText = urls.asScala.foldLeft(text)((text, url) => text.replace(url.getOriginalUrl, ""))
    cleanedText
  }
}
