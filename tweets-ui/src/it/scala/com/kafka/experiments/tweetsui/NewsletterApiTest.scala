package com.kafka.experiments.tweetsui

import cats.effect.IO
import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{RatedData, Tweet}
import com.dimafeng.testcontainers.ForEachTestContainer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.kafka.experiments.shared.{ArticleTweet, AudioTweet}
import com.kafka.experiments.tweetsui.Decoders._
import com.kafka.experiments.tweetsui.Encoders._
import com.kafka.experiments.tweetsui.NewsletterApiTest._
import com.kafka.experiments.tweetsui.api.{MoveTweetsToNewsletter, NewsletterApi, UpdateNewsletterTweet}
import com.kafka.experiments.tweetsui.client.{GithubClient, MediumClient, YoutubeClient}
import com.kafka.experiments.tweetsui.client.sendgrid.SendGridClient
import com.kafka.experiments.tweetsui.config._
import com.kafka.experiments.tweetsui.newsletter.{FreeMarkerGenerator, NewsletterBuilder, NewsletterTweet}
import com.kafka.experiments.tweetsui.score.ScoringService
import org.http4s._
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits.{http4sLiteralsSyntax, _}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pureconfig.ConfigSource

import java.time.Instant.now
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import pureconfig.generic.auto._

class NewsletterApiTest
    extends AnyFlatSpec
    with ForEachTestContainer
    with BeforeAndAfterEach
    with Matchers
    with MongoDatabase
    with MockSendGrid
    with MockYoutubeApi {

  private val config = ConfigSource.default.loadOrThrow[GlobalConfig]

  private val sendGridConfig = SendGridConfig(mockSendGridUrl, "key", 11, List("id"), 22)
  private val youtubeConfig = YoutubeConfig(mockYoutubeUrl, "key")

  private val newsletterBuilder =
    new NewsletterBuilder(mongoService, new FreeMarkerGenerator(config.freemarker), config.newsletter)
  private var httpClient: Client[IO] = _
  private var githubClient: GithubClient = _
  private var mediumClient: MediumClient = _
  private var twitterRestClient: MockedTwitterRestClient = _
  private var scoringService: ScoringService = _
  private var sendGridClient: SendGridClient = _
  private var youtubeClient: YoutubeClient = _
  private var api: HttpApp[IO] = _

  override def beforeEach: Unit = {
    super.beforeEach()
    httpClient = BlazeClientBuilder[IO](global).allocated.unsafeRunSync()._1
    githubClient = GithubClient(config.github, httpClient)
    mediumClient = MediumClient(httpClient)
    twitterRestClient = new MockedTwitterRestClient()
    youtubeClient = YoutubeClient(youtubeConfig, httpClient)
    scoringService = ScoringService(config.score, githubClient, mediumClient, twitterRestClient, youtubeClient)
    sendGridClient = SendGridClient(sendGridConfig, httpClient)
    api = new NewsletterApi(newsletterBuilder, mongoService, scoringService, sendGridClient).api().orNotFound
  }

  "Newsletter API" should "be used to prepare and create the email draft in SendGrid" in {
    val tweet = ArticleTweet("124142314", "Some good Kafka stuff", "http://medium.com/123445", "mlmenace", "1609020620")
    val tweet2 = AudioTweet("124142334", "Even move Kafka stuff", "http://medium.com/789445", "justin", "1605020620")
    mongoService.createTweet(tweet, Article).unsafeRunSync()
    mongoService.createTweet(tweet2, Audio).unsafeRunSync()
    val tweetsToInclude = MoveTweetsToNewsletter(
      Map(
        "article" -> List("124142314"),
        "audio" -> List("124142334")
      )
    )

    // Prepare the tweets that should be included in the Newsletter
    val response1 = api.run(Request(method = Method.PUT, uri = uri"/newsletter/prepare").withEntity(tweetsToInclude))
    check[String](response1, Status.Ok, None)

    // Check the newsletter content
    val response2 = api.run(Request(method = Method.GET, uri = uri"/newsletter/included"))
    check[Seq[NewsletterTweet]](
      response2,
      Status.Ok,
      Some(
        List(
          NewsletterTweet(
            "124142334",
            "justin",
            "Even move Kafka stuff",
            "http://medium.com/789445",
            "1605020620",
            "audio"
          ),
          NewsletterTweet(
            "124142314",
            "mlmenace",
            "Some good Kafka stuff",
            "http://medium.com/123445",
            "1609020620",
            "article"
          )
        )
      )
    )

    // Check the Newsletter appearance
    val response3 = api.run(Request(method = Method.GET, uri = uri"/newsletter/html"))
    val htmlContent = check[String](response3, Status.Ok, None).as[String].unsafeRunSync()
    htmlContent should include("Some good Kafka stuff")
    htmlContent should include("Even move Kafka stuff")

    // Create Email draft
    sendgridApi.stubFor(
      post(urlPathEqualTo("/v3/marketing/singlesends"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBody(s"""{"id":"${UUID.randomUUID()}"}""")
        )
    )
    val response4 = api.run(Request(method = Method.POST, uri = uri"/newsletter/create"))
    check[String](response4, Status.Ok, None)
    sendgridApi.verify(
      postRequestedFor(urlEqualTo("/v3/marketing/singlesends")).withHeader("Content-Type", equalTo("application/json"))
    )
  }

  "Newsletter API" should "reset newsletter data" in {
    val tweet = ArticleTweet("124142314", "Some good Kafka stuff", "http://medium.com/123445", "mlmenace", "1609020620")
    val tweet2 = AudioTweet("124142334", "Even move Kafka stuff", "http://medium.com/789445", "justin", "1605020620")
    mongoService.createTweet(tweet, Article).unsafeRunSync()
    mongoService.createTweet(tweet2, Audio).unsafeRunSync()
    val tweetsToInclude = MoveTweetsToNewsletter(
      Map(
        "article" -> List("124142314"),
        "audio" -> List("124142334")
      )
    )

    val response1 = api.run(Request(method = Method.PUT, uri = uri"/newsletter/prepare").withEntity(tweetsToInclude))
    check[String](response1, Status.Ok, None)
    val response2 = api.run(Request(method = Method.DELETE, uri = uri"/newsletter/reset"))
    check[String](response2, Status.Ok, None)
    val response3 = api.run(Request(method = Method.GET, uri = uri"/newsletter/included"))
    check[Seq[NewsletterTweet]](response3, Status.Ok, Some(List[NewsletterTweet]()))

    verifyZeroInteractionsWithSendGridServer()
  }

  "Newsletter API" should "delete a specifc tweet in newsletter" in {
    val tweet = ArticleTweet("124142314", "Some good Kafka stuff", "http://medium.com/123445", "mlmenace", "1609020620")
    val tweet2 = AudioTweet("124142334", "Even move Kafka stuff", "http://medium.com/789445", "justin", "1605020620")
    mongoService.createTweet(tweet, Article).unsafeRunSync()
    mongoService.createTweet(tweet2, Audio).unsafeRunSync()
    val tweetsToInclude = MoveTweetsToNewsletter(
      Map(
        "article" -> List("124142314"),
        "audio" -> List("124142334")
      )
    )

    val response1 = api.run(Request(method = Method.PUT, uri = uri"/newsletter/prepare").withEntity(tweetsToInclude))
    check[String](response1, Status.Ok, None)
    val response2 = api.run(Request(method = Method.DELETE, uri = uri"/newsletter/tweet/124142314"))
    check[String](response2, Status.Ok, None)
    val response3 = api.run(Request(method = Method.GET, uri = uri"/newsletter/included"))
    check[Seq[NewsletterTweet]](
      response3,
      Status.Ok,
      Some(
        List[NewsletterTweet](
          NewsletterTweet(
            "124142334",
            "justin",
            "Even move Kafka stuff",
            "http://medium.com/789445",
            "1605020620",
            "audio"
          )
        )
      )
    )
  }

  "Newsletter API" should "change a tweet category and favourite status" in {
    val tweet = ArticleTweet("124142314", "Some good Kafka stuff", "http://medium.com/123445", "mlmenace", "1609020620")
    mongoService.createTweet(tweet, Article).unsafeRunSync()
    val tweetsToInclude = MoveTweetsToNewsletter(
      Map(
        "article" -> List(tweet.id)
      )
    )

    val response1 = api.run(Request(method = Method.PUT, uri = uri"/newsletter/prepare").withEntity(tweetsToInclude))
    check[String](response1, Status.Ok, None)
    val response2 = api.run(
      Request(method = Method.PUT, uri = uri"/newsletter/tweet")
        .withEntity(UpdateNewsletterTweet(tweet.id, category = Some("audio"), favourite = Some(true)))
    )
    check[String](response2, Status.Ok, None)
    val response3 = api.run(Request(method = Method.GET, uri = uri"/newsletter/included"))
    check[Seq[NewsletterTweet]](
      response3,
      Status.Ok,
      Some(
        List[NewsletterTweet](
          NewsletterTweet(
            tweet.id,
            "mlmenace",
            "Some good Kafka stuff",
            "http://medium.com/123445",
            "1609020620",
            "audio",
            favourite = true
          )
        )
      )
    )
  }

  "Newsletter API" should "calculate scores" in {
    val tweet = ArticleTweet(
      "124142314",
      "Some good Kafka stuff",
      "https://www.youtube.com/watch?v=cvu53CnZmGI",
      "mlmenace",
      "1609020620"
    )
    mongoService.createTweet[ArticleTweet](tweet, Article).unsafeRunSync()
    val tweetsToInclude = MoveTweetsToNewsletter(Map("article" -> List("124142314")))
    twitterRestClient.setMetadata(Seq(baseTweet))

    youtubeApi.stubFor(
      get(urlPathEqualTo("/videos"))
        .withQueryParam("id", equalTo("cvu53CnZmGI"))
        .withQueryParam("part", equalTo("statistics"))
        .withQueryParam("part", equalTo("contentDetails"))
        .withQueryParam("key", equalTo("key"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBody(s"""{
                         |    "kind": "youtube#videoListResponse",
                         |    "etag": "qFqOIDNWKzz7AgfleDHqmpV6fEs",
                         |    "items": [
                         |        {
                         |            "kind": "youtube#video",
                         |            "etag": "6M8RAA-jCufKBeEplft6r-e7048",
                         |            "id": "tTx8q4oPx7E",
                         |            "contentDetails": {
                         |                "duration": "PT22M",
                         |                "dimension": "2d",
                         |                "definition": "hd",
                         |                "caption": "false",
                         |                "licensedContent": false,
                         |                "contentRating": {},
                         |                "projection": "rectangular"
                         |            },
                         |            "statistics": {
                         |                "viewCount": "0",
                         |                "likeCount": "0",
                         |                "dislikeCount": "0",
                         |                "favoriteCount": "0"
                         |            }
                         |        }
                         |    ],
                         |    "pageInfo": {
                         |        "totalResults": 1,
                         |        "resultsPerPage": 1
                         |    }
                         |}""".stripMargin)
        )
    )

    val response1 = api.run(Request(method = Method.PUT, uri = uri"/newsletter/prepare").withEntity(tweetsToInclude))
    check[String](response1, Status.Ok, None)
    val response2 = api.run(Request(method = Method.PUT, uri = uri"/newsletter/score"))
    check[String](response2, Status.Ok, Some("Scored"))
    val response3 = api.run(Request(method = Method.GET, uri = uri"/newsletter/included"))

    val finalResponse = check[Seq[NewsletterTweet]](
      response3,
      Status.Ok,
      None
    )
    val tweetWithScore: NewsletterTweet = finalResponse.as[Seq[NewsletterTweet]].unsafeRunSync().head
    tweetWithScore.score.toInt shouldBe 55
  }

  def check[A](actual: IO[Response[IO]], expectedStatus: Status, expectedBody: Option[A])(implicit
      ev: EntityDecoder[IO, A]
  ): Response[IO] = {
    val actualResp = actual.unsafeRunSync()
    actualResp.status shouldBe expectedStatus
    expectedBody.foreach(expected => actualResp.as[A].unsafeRunSync() shouldBe expected)
    actualResp
  }

}

object NewsletterApiTest {

  val config: ScoringConfig = ScoringConfig(
    GithubScoringConfig(
      stars = ScaledScoreConfig(1, Map("1" -> 100, "10" -> 200)),
      watchers = ScaledScoreConfig(2, Map("1" -> 100, "10" -> 200))
    ),
    MediumScoringConfig(claps = ScaledScoreConfig(1, Map("1" -> 100, "10" -> 200))),
    SourceConfig(List("badsource", "otherbadsource")),
    TwitterScoringConfig(
      favourites = ScaledScoreConfig(1, Map("1" -> 100, "10" -> 1000)),
      followers = ScaledScoreConfig(1, Map("20" -> 200, "200" -> 2000)),
      retweets = ScaledScoreConfig(1, Map("300" -> 300, "3000" -> 3000))
    ),
    YoutubeScoringConfig(
      dislikes = ScaledScoreConfig(-1, Map("0" -> 0, "1" -> 100, "10" -> 1000)),
      duration = ScaledScoreConfig(1, Map("0" -> 0, "20" -> 200, "200" -> 2000)),
      favourites = ScaledScoreConfig(2, Map("0" -> 0, "300" -> 300, "3000" -> 3000)),
      likes = ScaledScoreConfig(3, Map("0" -> 0, "400" -> 400, "4000" -> 4000)),
      views = ScaledScoreConfig(4, Map("0" -> 0, "500" -> 500, "5000" -> 5000))
    )
  )

  val baseTweet: Tweet =
    Tweet(
      created_at = now(),
      favorite_count = 2,
      id = 124142314,
      id_str = "124142314",
      source = "",
      text = "Some text",
      user = None
    )

  class MockedTwitterRestClient() extends TwitterRestClient(consumerToken = null, accessToken = null) {

    var metadata: Seq[Tweet] = _

    def setMetadata(metadata: Seq[Tweet]): Unit = {
      this.metadata = metadata
    }

    override def tweetLookup(ids: Long*): Future[RatedData[Seq[Tweet]]] = {
      Future(RatedData(null, this.metadata))
    }
  }
}
