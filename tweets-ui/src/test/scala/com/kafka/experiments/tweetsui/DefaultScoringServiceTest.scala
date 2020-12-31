package com.kafka.experiments.tweetsui

import cats.effect.{ContextShift, IO}
import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{RatedData, Tweet, User}
import com.kafka.experiments.tweetsui.DefaultScoringServiceTest._
import com.kafka.experiments.tweetsui.config.ScoringConfig
import com.kafka.experiments.tweetsui.newsletter.NewsletterTweet
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant.now
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DefaultScoringServiceTest extends AnyFlatSpec with BeforeAndAfterEach with Matchers {

  implicit val contextShift: ContextShift[IO] = IO.contextShift(global)

  var scoringService: ScoringService = _

  "Score" should "be calculated based on tweet favourites" in {
    val metadata = Seq(
      baseTweet.copy(id_str = "1", favorite_count = 0),
      baseTweet.copy(id_str = "2", favorite_count = 2),
      baseTweet.copy(id_str = "3", favorite_count = 12)
    )
    scoringService = new DefaultScoringService(config, new MockedTwitterRestClient(metadata))

    val tweets = List(
      baseNewsTweet.copy(id = "1"),
      baseNewsTweet.copy(id = "2"),
      baseNewsTweet.copy(id = "3")
    )
    val scoredTweets = scoringService.calculateScores(tweets).unsafeRunSync()

    scoredTweets.map(_.score) shouldBe List(Some(0), Some(100), Some(1000))
  }

  "Score" should "be calculated based on user followers" in {
    val metadata = Seq(
      baseTweet.copy(id_str = "1", user = baseTweet.user.map(_.copy(followers_count = 0))),
      baseTweet.copy(id_str = "2", user = baseTweet.user.map(_.copy(followers_count = 22))),
      baseTweet.copy(id_str = "3", user = baseTweet.user.map(_.copy(followers_count = 202)))
    )
    scoringService = new DefaultScoringService(config, new MockedTwitterRestClient(metadata))

    val tweets = List(
      baseNewsTweet.copy(id = "1"),
      baseNewsTweet.copy(id = "2"),
      baseNewsTweet.copy(id = "3")
    )
    val scoredTweets = scoringService.calculateScores(tweets).unsafeRunSync()

    scoredTweets.map(_.score) shouldBe List(Some(0), Some(200), Some(2000))
  }

  "Score" should "be calculated based on tweet retweets" in {
    val metadata = Seq(
      baseTweet.copy(id_str = "1", retweet_count = 0),
      baseTweet.copy(id_str = "2", retweet_count = 302),
      baseTweet.copy(id_str = "3", retweet_count = 3002)
    )
    scoringService = new DefaultScoringService(config, new MockedTwitterRestClient(metadata))

    val tweets = List(
      baseNewsTweet.copy(id = "1"),
      baseNewsTweet.copy(id = "2"),
      baseNewsTweet.copy(id = "3")
    )
    val scoredTweets = scoringService.calculateScores(tweets).unsafeRunSync()

    scoredTweets.map(_.score) shouldBe List(Some(0), Some(300), Some(3000))
  }

  "Score" should "be calculated based on all data" in {
    val metadata = Seq(
      baseTweet.copy(
        id_str = "1",
        favorite_count = 0,
        retweet_count = 0,
        user = baseTweet.user.map(_.copy(followers_count = 0))
      ),
      baseTweet.copy(
        id_str = "2",
        favorite_count = 2,
        retweet_count = 302,
        user = baseTweet.user.map(_.copy(followers_count = 22))
      ),
      baseTweet.copy(
        id_str = "3",
        favorite_count = 12,
        retweet_count = 3002,
        user = baseTweet.user.map(_.copy(followers_count = 202))
      )
    )
    scoringService = new DefaultScoringService(config, new MockedTwitterRestClient(metadata))

    val tweets = List(
      baseNewsTweet.copy(id = "1"),
      baseNewsTweet.copy(id = "2"),
      baseNewsTweet.copy(id = "3")
    )
    val scoredTweets = scoringService.calculateScores(tweets).unsafeRunSync()

    scoredTweets.map(_.score) shouldBe List(Some(0), Some(600), Some(6000))
  }

  class MockedTwitterRestClient(metadata: Seq[Tweet])
      extends TwitterRestClient(consumerToken = null, accessToken = null) {

    override def tweetLookup(ids: Long*): Future[RatedData[Seq[Tweet]]] = {
      Future(RatedData(null, metadata))
    }
  }
}

object DefaultScoringServiceTest {

  val config: ScoringConfig = ScoringConfig(
    favourites = Map("1" -> 100, "10" -> 1000),
    followers = Map("20" -> 200, "200" -> 2000),
    retweets = Map("300" -> 300, "3000" -> 3000)
  )

  val user: User = User(
    created_at = now(),
    favourites_count = 0,
    followers_count = 0,
    friends_count = 0,
    id = 1L,
    id_str = "1",
    lang = "en",
    listed_count = 0,
    name = "",
    profile_background_color = "",
    profile_background_image_url = "",
    profile_background_image_url_https = "",
    profile_image_url = null,
    profile_image_url_https = null,
    profile_link_color = "",
    profile_location = None,
    profile_sidebar_border_color = "",
    profile_sidebar_fill_color = "",
    profile_text_color = "",
    screen_name = "",
    statuses_count = 0
  )

  val baseTweet: Tweet =
    Tweet(created_at = now(), id = 12345, id_str = "12345", source = "", text = "Some text", user = Some(user))
  val baseNewsTweet: NewsletterTweet = NewsletterTweet("12345", "sam", "Some text", "", "", "article")
}
