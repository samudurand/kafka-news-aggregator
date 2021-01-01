package com.kafka.experiments.tweetsui.config

case class GlobalConfig(
    freemarker: FreeMarkerConfig,
    mongodb: MongodbConfig,
    score: ScoringConfig,
    sendgrid: SendGridConfig,
    server: ServerConfig,
    youtube: YoutubeConfig
)

case class ScoringConfig(twitter: TwitterConfig)

case class TwitterConfig(favourites: ScaledScoreConfig, followers: ScaledScoreConfig, retweets: ScaledScoreConfig)

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

case class YoutubeConfig(baseUrl: String, apiKey: String)
