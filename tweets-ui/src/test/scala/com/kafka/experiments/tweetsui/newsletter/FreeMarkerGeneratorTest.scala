package com.kafka.experiments.tweetsui.newsletter

import com.kafka.experiments.tweetsui.config.FreeMarkerConfig
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.jdk.CollectionConverters._

class FreeMarkerGeneratorTest extends AnyFlatSpec with Matchers {

  private val fmGenerator = new FreeMarkerGenerator(FreeMarkerConfig(None))
  private val tweet = NewsletterTweet("12345", "someguy", "hello there", "http://link", "1314325356", "Video")

  "Generator" should "generate newsletter with all categories" in {
    val data = Map(
      "listArticles" -> List(tweet).asJava,
      "listAudios" -> List(tweet).asJava,
      "listVersions" -> List(tweet).asJava,
      "listVideos" -> List(tweet).asJava,
      "listOthers" -> List(tweet).asJava
    )

    val html = fmGenerator.generateHtml(data)

    html should include ("Articles</h3>")
    html should include ("Podcasts</h3>")
    html should include ("Videos</h3>")
    html should include ("Releases</h3>")
    html should include ("Others</h3>")
  }

  "Generator" should "generate newsletter with some categories but not all" in {
    val data = Map(
      "listArticles" -> List(tweet).asJava,
      "listAudios" -> List(tweet).asJava
    )

    val html = fmGenerator.generateHtml(data)

    html should include ("Articles</h3>")
    html should include ("Podcasts</h3>")
    html should not include ("Videos</h3>")
    html should not include ("Releases</h3>")
    html should not include ("Others</h3>")
  }

  "Generator" should "generate newsletter without any category if all lists empty" in {
    val data = Map(
      "listArticles" -> List().asJava,
      "listAudios" -> List().asJava,
      "listVersions" -> List().asJava,
      "listVideos" -> List().asJava,
      "listOthers" -> List().asJava
    )

    val html = fmGenerator.generateHtml(data)

    html should not include ("Articles</h3>")
    html should not include ("Podcasts</h3>")
    html should not include ("Videos</h3>")
    html should not include ("Releases</h3>")
    html should not include ("Others</h3>")
  }

  "Generator" should "generate newsletter without any category if no data provided" in {
    val data = Map[String, AnyRef]()
    val html = fmGenerator.generateHtml(data)

    html should not include ("Articles</h3>")
    html should not include ("Podcasts</h3>")
    html should not include ("Videos</h3>")
    html should not include ("Releases</h3>")
    html should not include ("Others</h3>")
  }

}
