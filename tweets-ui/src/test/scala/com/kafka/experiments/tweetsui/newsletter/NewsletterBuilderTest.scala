package com.kafka.experiments.tweetsui.newsletter

import cats.effect.IO
import com.kafka.experiments.tweetsui.MongoService
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.jdk.CollectionConverters._

class NewsletterBuilderTest extends AnyFlatSpec with Matchers with MockFactory with BeforeAndAfterEach {

  var fmGenerator: FreeMarkerGenerator = _
  var mongoService: MongoService = _
  var builder: NewsletterBuilder = _

  override def beforeEach(): Unit = {
    fmGenerator = mock[FreeMarkerGenerator]
    mongoService = mock[MongoService]
    builder = new NewsletterBuilder(mongoService, fmGenerator)
  }

  "Newsletter Builder" should "build newsletter" in {
    (mongoService.tweetsForNewsletter _)
      .expects()
      .returning(
        IO.pure(
          List(
            CompleteNewsletterTweet(
              "1604688491000",
              "user",
              "Some tweet about #Kafka, http://urltoremove.com",
              "http://url.com",
              "12314513543",
              "article"
            ),
            CompleteNewsletterTweet(
              "1604444491000",
              "user2",
              "Some other tweet http://urltoremove.com about #Kafka",
              "http://url.com",
              "12222213543",
              "audio"
            )
          )
        )
      )

    (fmGenerator.generateHtml _).expects(
      Map(
        "listArticles" -> List(
          CompleteNewsletterTweet(
            "1604688491000",
            "user",
            "Some tweet about #Kafka, ",
            "http://url.com",
            "12314513543",
            "article"
          )
        ).asJava,
        "listAudios" -> List(
          CompleteNewsletterTweet(
              "1604444491000",
              "user2",
              "Some other tweet  about #Kafka",
              "http://url.com",
              "12222213543",
              "audio"
            )
        ).asJava
      )
    ).returning("generated")

    builder.buildNewsletter().unsafeRunSync() shouldBe "generated"
  }

}
