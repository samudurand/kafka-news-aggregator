package com.kafka.experiments.tweetsui

import cats.effect.{ContextShift, IO}
import com.danielasfregola.twitter4s.entities.{Tweet, User}
import com.kafka.experiments.tweetsui.DefaultScoringServiceTest._
import com.kafka.experiments.tweetsui.config.{ScaledScoreConfig, ScoringConfig, TwitterScoringConfig}
import com.kafka.experiments.tweetsui.newsletter.NewsletterTweet
import com.kafka.experiments.tweetsui.score.{DefaultScoringService, ScoringService}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant.now
import scala.concurrent.ExecutionContext.Implicits.global

class DefaultScoringServiceTest extends AnyFlatSpec with BeforeAndAfterEach with Matchers {

  implicit val contextShift: ContextShift[IO] = IO.contextShift(global)

  var scoringService: ScoringService = _

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
    scoringService = new DefaultScoringService(config, new MockedTwitterRestClient(metadata), null)

    val tweets = List(
      baseNewsTweet.copy(id = "1"),
      baseNewsTweet.copy(id = "2"),
      baseNewsTweet.copy(id = "3")
    )
    val scoredTweets = scoringService.calculateScores(tweets).unsafeRunSync()

    scoredTweets(0).score.toInt shouldBe 0
    scoredTweets(1).score.toInt shouldBe 200
    scoredTweets(2).score.toInt shouldBe 2000
  }
}

object DefaultScoringServiceTest {

  val config: ScoringConfig = ScoringConfig(TwitterScoringConfig(
    favourites = ScaledScoreConfig(1, Map("0" -> 0, "1" -> 100, "10" -> 1000)),
    followers = ScaledScoreConfig(1, Map("0" -> 0, "20" -> 200, "200" -> 2000)),
    retweets = ScaledScoreConfig(1, Map("0" -> 0, "300" -> 300, "3000" -> 3000))), null
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
