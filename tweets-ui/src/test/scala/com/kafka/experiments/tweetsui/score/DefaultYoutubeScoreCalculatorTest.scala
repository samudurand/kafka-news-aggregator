package com.kafka.experiments.tweetsui.score

import cats.effect.IO
import com.kafka.experiments.tweetsui.client.{VideoMetadata, YoutubeClient}
import com.kafka.experiments.tweetsui.config.{ScaledScoreConfig, YoutubeScoringConfig}
import com.kafka.experiments.tweetsui.newsletter.NewsletterTweet
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.DurationInt

class DefaultYoutubeScoreCalculatorTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach with MockFactory {

  private val config = YoutubeScoringConfig(
    dislikes = ScaledScoreConfig(1, Map("1" -> 100, "10" -> 500)),
    duration = ScaledScoreConfig(2, Map("10" -> 100, "30" -> 500)),
    favourites = ScaledScoreConfig(3, Map("1" -> 100, "10" -> 500)),
    likes = ScaledScoreConfig(4, Map("1" -> 100, "10" -> 500)),
    views = ScaledScoreConfig(5, Map("1" -> 100, "10" -> 500))
  )
  private var youtubeClient: YoutubeClient = _
  private var scoreCalculator: YoutubeScoreCalculator = _

  override def beforeEach(): Unit = {
    youtubeClient = mock[YoutubeClient]
    scoreCalculator = new DefaultYoutubeScoreCalculator(config, youtubeClient)
  }

  "Score calculator" should "calculate youtube scores" in {
    (youtubeClient.videoData _)
      .expects("cvu53CnZmGI")
      .returning(IO.pure(Some(VideoMetadata("cvu53CnZmGI", 2, 12.minutes, 20, 5, 15))))

    val scoredTweets = scoreCalculator
      .calculate(
        List(
          NewsletterTweet("123", "sam", "text", "https://youtube.com?v=cvu53CnZmGI", "1231342343", "video"),
          NewsletterTweet("456", "sam", "text", "https://dailymotion.com", "1231342343", "video")
        )
      )
      .unsafeRunSync()

    scoredTweets("123") shouldBe List(
      Score("Youtube Dislikes", 100, 1),
      Score("Youtube Duration", 100, 2),
      Score("Youtube Favourites", 500, 3),
      Score("Youtube Likes", 100, 4),
      Score("Youtube Views", 500, 5)
    )
    scoredTweets("456") shouldBe List()
  }

}
