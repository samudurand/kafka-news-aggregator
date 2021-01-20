package com.kafka.experiments.tweetsui.config

case class GlobalConfig(
    freemarker: FreeMarkerConfig,
    github: GithubConfig,
    mongodb: MongodbConfig,
    newsletter: NewsletterConfig,
    score: ScoringConfig,
    sendgrid: SendGridConfig,
    server: ServerConfig,
    youtube: YoutubeConfig
)

case class ScoringConfig(
    github: GithubScoringConfig,
    medium: MediumScoringConfig,
    sources: SourceConfig,
    twitter: TwitterScoringConfig,
    youtube: YoutubeScoringConfig
)

case class SourceConfig(poor: List[String])

case class TwitterScoringConfig(
    favourites: ScaledScoreConfig,
    followers: ScaledScoreConfig,
    retweets: ScaledScoreConfig
)

case class ScaledScoreConfig(factor: Int, scale: Map[String, Int]) {
  val getScale: Map[Int, Int] = scale.map { case (key, value) => key.toInt -> value }
}

case class MongodbConfig(
    host: String,
    port: Int
)

case class ServerConfig(host: String, port: Int)

case class SendGridConfig(baseUrl: String, apiKey: String, senderId: Int, listIds: List[String], unsubscribeListId: Int)

case class FreeMarkerConfig(templatesFolderSystemPath: Option[String])

case class GithubConfig(baseUrl: String)

case class GithubScoringConfig(stars: ScaledScoreConfig, watchers: ScaledScoreConfig)

case class MediumScoringConfig(claps: ScaledScoreConfig)

case class YoutubeConfig(baseUrl: String, apiKey: String)

case class YoutubeScoringConfig(
    dislikes: ScaledScoreConfig,
    duration: ScaledScoreConfig,
    favourites: ScaledScoreConfig,
    likes: ScaledScoreConfig,
    views: ScaledScoreConfig
)

case class NewsletterConfig(maxByCategory: Int)