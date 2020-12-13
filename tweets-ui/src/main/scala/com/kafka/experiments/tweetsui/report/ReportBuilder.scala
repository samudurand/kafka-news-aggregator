package com.kafka.experiments.tweetsui.report

import com.kafka.experiments.tweetsui.MongoService

import scala.jdk.CollectionConverters._

class ReportBuilder(mongoService: MongoService) {

  val freeMarkerGenerator = new FreeMarkerGenerator()

  def buildReport(): String = {
//    mongoService.tweets()

    val map = Map("videos" -> List("1", "2").asJava)
    freeMarkerGenerator.generateHtml(map)
  }

}
