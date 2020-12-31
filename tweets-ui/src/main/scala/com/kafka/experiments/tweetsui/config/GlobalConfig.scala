package com.kafka.experiments.tweetsui.config

case class GlobalConfig(
    freemarker: FreeMarkerConfig,
    mongodb: MongodbConfig,
    score: ScoringConfig,
    sendgrid: SendGridConfig,
    server: ServerConfig,
    youtube: YoutubeConfig
)

case class ScoringConfig(
    favourites: Map[String, Int],
    followers: Map[String, Int],
    retweets: Map[String, Int]
) {
  val scaleFavourites: Map[Int, Int] = favourites.map { case (key, value) => key.toInt -> value }
  val scaleFollowers: Map[Int, Int] = followers.map { case (key, value) => key.toInt -> value }
  val scaleRetweets: Map[Int, Int] = retweets.map { case (key, value) => key.toInt -> value }
}

case class MongodbConfig(
    host: String,
    port: Int
)

case class ServerConfig(host: String, port: Int)

case class SendGridConfig(baseUrl: String, apiKey: String, senderId: Int, listIds: List[String], unsubscribeListId: Int)

case class FreeMarkerConfig(templatesFolderSystemPath: Option[String])

case class YoutubeConfig(baseUrl: String, apiKey: String)