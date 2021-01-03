package com.kafka.experiments.tweetsui.score

import cats.effect.IO
import cats.implicits._
import com.kafka.experiments.tweetsui.client.YoutubeClient
import com.kafka.experiments.tweetsui.config.{ScaledScoreConfig, YoutubeScoringConfig}
import com.kafka.experiments.tweetsui.newsletter.NewsletterTweet
import com.typesafe.scalalogging.StrictLogging

trait YoutubeScoreCalculator extends ScoreCalculator

object YoutubeScoreCalculator {

  def apply(config: YoutubeScoringConfig, youtubeClient: YoutubeClient): YoutubeScoreCalculator = {
    new DefaultYoutubeScoreCalculator(config, youtubeClient)
  }
}

class DefaultYoutubeScoreCalculator(config: YoutubeScoringConfig, youtubeClient: YoutubeClient)
    extends YoutubeScoreCalculator
    with StrictLogging {

  private val youtubeDomain = "youtube"
  private val youtubePlaylist = "/playlist"

  override def calculate(tweets: Seq[NewsletterTweet]): IO[Map[String, Seq[Score]]] = {
    tweets
      .map {
        case tweet if hasYoutubeVideoUrl(tweet) =>
          calculateYoutubeScores(tweet).map {
            case Some(scores) => tweet.id -> scores
            case None         => tweet.id -> List()
          }
        case tweet => IO(tweet.id -> List[Score]())
      }
      .toList
      .sequence
      .map(_.toMap)
  }

  private def calculateYoutubeScores(tweet: NewsletterTweet): IO[Option[List[Score]]] = {
    extractVideoId(tweet.url)
      .map(videoId =>
        youtubeClient
          .videoData(videoId)
          .map(metadataOpt =>
            for {
              metadata <- metadataOpt
              dislikesScore = calculateScaledScore("Youtube Dislikes", config.dislikes, metadata.dislikeCount)
              durationScore = calculateScaledScore("Youtube Duration", config.duration, metadata.duration.toMinutes)
              favouritesScore = calculateScaledScore("Youtube Favourites", config.favourites, metadata.favouriteCount)
              likesScore = calculateScaledScore("Youtube Likes", config.likes, metadata.likeCount)
              viewsScore = calculateScaledScore("Youtube Views", config.views, metadata.viewCount)
            } yield List(dislikesScore, durationScore, favouritesScore, likesScore, viewsScore)
          )
      )
      .sequence
      .map(_.flatten)
  }

  private def calculateScaledScore(name: String, config: ScaledScoreConfig, value: Long): Score = {
    val score = calculateCountScore(config.getScale, value)
    Score(name, score, config.factor)
  }

  private def hasYoutubeVideoUrl(tweet: NewsletterTweet) = {
    tweet.url.contains(youtubeDomain) && !tweet.url.contains(youtubePlaylist)
  }

  private def extractVideoId(url: String) = {
    try {
      val idPossiblyWithAmp = url.split("v=")(1)
      val ampIndex = idPossiblyWithAmp.indexOf('&')
      if (ampIndex > -1) {
        Some(idPossiblyWithAmp.substring(0, ampIndex))
      } else {
        Some(idPossiblyWithAmp)
      }
    } catch {
      case ex: Throwable =>
        logger.error(s"Unable to extract video ID from url [$url]", ex)
        None
    }
  }
}
