package com.kafka.experiments.tweetsui

import cats.effect.{ContextShift, IO}
import com.danielasfregola.twitter4s.entities.{Tweet, User}
import com.kafka.experiments.tweetsui.DefaultScoringServiceTest._
import com.kafka.experiments.tweetsui.client.{GithubClient, RepoMetadata, VideoMetadata, YoutubeClient}
import com.kafka.experiments.tweetsui.config.{GithubScoringConfig, ScaledScoreConfig, ScoringConfig, SourceConfig, TwitterScoringConfig, YoutubeScoringConfig}
import com.kafka.experiments.tweetsui.newsletter.NewsletterTweet
import com.kafka.experiments.tweetsui.score.{DefaultScoringService, ScoringService}
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant.now
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, MINUTES}

class DefaultScoringServiceTest extends AnyFlatSpec with BeforeAndAfterEach with Matchers with MockFactory {

  implicit val contextShift: ContextShift[IO] = IO.contextShift(global)

  var githubClient: GithubClient = _
  var youtubeClient: YoutubeClient = _
  var scoringService: ScoringService = _

  override def beforeEach(): Unit = {
    super.beforeEach()
    githubClient = mock[GithubClient]
    youtubeClient = mock[YoutubeClient]
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
      ),
      baseTweet.copy(
        id_str = "4",
        favorite_count = 12,
        retweet_count = 3002,
        user = baseTweet.user.map(_.copy(followers_count = 202))
      ),
      baseTweet.copy(
        id_str = "5",
        favorite_count = 0,
        retweet_count = 0,
        user = baseTweet.user.map(_.copy(followers_count = 0))
      )
    )
    scoringService = new DefaultScoringService(config, githubClient, new MockedTwitterRestClient(metadata), youtubeClient)
    (youtubeClient.videoData _)
      .expects("cvu53CnZmGI")
      .returning(IO.pure(Some(VideoMetadata("cvu53CnZmGI", 2, Duration(21, MINUTES), 301, 401, 501))))
    (githubClient.retrieveRepoMetadata _)
      .expects("https://github.com/samudurand/kafka-news-aggregator")
      .returning(IO.pure(Some(RepoMetadata(2, 80))))

    val tweets = List(
      baseNewsTweet.copy(id = "1"),
      baseNewsTweet.copy(id = "2"),
      baseNewsTweet.copy(id = "3"),
      baseNewsTweet.copy(id = "4", url = "https://www.youtube.com/watch?v=cvu53CnZmGI&list=PLIivdWyY5sqKwse&index=1"),
      baseNewsTweet.copy(id = "5", url = "https://github.com/samudurand/kafka-news-aggregator")
    )
    val scoredTweets = scoringService.calculateScores(tweets).unsafeRunSync().sortBy(_.id)

    scoredTweets(0).score.toInt shouldBe 0
    scoredTweets(1).score.toInt shouldBe 233
    scoredTweets(2).score.toInt shouldBe 2333
    scoredTweets(3).score.toInt shouldBe 1053
    scoredTweets(4).score.toInt shouldBe 56
  }
}

object DefaultScoringServiceTest {

  val config: ScoringConfig = ScoringConfig(
    GithubScoringConfig(
      stars = ScaledScoreConfig(1, Map("1" -> 100, "10" -> 200)),
      watchers = ScaledScoreConfig(2, Map("1" -> 100, "10" -> 200))
    ),
    SourceConfig(List("badsource", "otherbadsource")),
    TwitterScoringConfig(
      favourites = ScaledScoreConfig(1, Map("0" -> 0, "1" -> 100, "10" -> 1000)),
      followers = ScaledScoreConfig(2, Map("0" -> 0, "20" -> 200, "200" -> 2000)),
      retweets = ScaledScoreConfig(3, Map("0" -> 0, "300" -> 300, "3000" -> 3000))
    ),
    YoutubeScoringConfig(
      dislikes = ScaledScoreConfig(-1, Map("0" -> 0, "1" -> 100, "10" -> 1000)),
      duration = ScaledScoreConfig(1, Map("0" -> 0, "20" -> 200, "200" -> 2000)),
      favourites = ScaledScoreConfig(2, Map("0" -> 0, "300" -> 300, "3000" -> 3000)),
      likes = ScaledScoreConfig(3, Map("0" -> 0, "400" -> 400, "4000" -> 4000)),
      views = ScaledScoreConfig(4, Map("0" -> 0, "500" -> 500, "5000" -> 5000))
    )
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
