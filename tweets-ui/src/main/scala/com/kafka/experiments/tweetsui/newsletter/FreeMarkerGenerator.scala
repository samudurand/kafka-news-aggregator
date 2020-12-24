package com.kafka.experiments.tweetsui.newsletter

import com.kafka.experiments.tweetsui.config.FreeMarkerConfig
import com.typesafe.scalalogging.StrictLogging
import freemarker.template.{Configuration, TemplateExceptionHandler}

import java.io.{File, StringWriter, Writer}
import scala.jdk.CollectionConverters._

class FreeMarkerGenerator(freeMarkerConfig: FreeMarkerConfig) extends StrictLogging {

  private val templateFolder = {
    freeMarkerConfig.templatesFolderSystemPath match {
      case Some(fileSystemPath) => new File(fileSystemPath)
      case _                    => new File(getClass.getClassLoader.getResource("newsletter-templates").getFile)
    }
  }

  private val fmConfig = freeMarkerConfiguration()

  def generateHtml(data: Map[String, AnyRef]): String = {
    val template = fmConfig.getTemplate("template.html")
    val out: Writer = new StringWriter()
    template.process(data.asJava, out)
    out.toString
  }

  private def freeMarkerConfiguration(): Configuration = {
    val cfg = new Configuration(Configuration.VERSION_2_3_30)
    cfg.setDirectoryForTemplateLoading(templateFolder)
    cfg.setDefaultEncoding("UTF-8")
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER)
    cfg.setLogTemplateExceptions(false)
    cfg.setWrapUncheckedExceptions(true)
    cfg.setFallbackOnNullLoopVariable(false)
    cfg
  }
}
