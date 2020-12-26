package com.kafka.experiments.tweetsui.config

case class GlobalConfig(
    freemarker: FreeMarkerConfig,
    mongodb: MongodbConfig,
    sendgrid: SendGridConfig,
    server: ServerConfig
)

case class MongodbConfig(
    collArticle: String,
    collAudio: String,
    collExcluded: String,
    collExaminate: String,
    collInteresting: String,
    collNewsletter: String,
    collPromotion: String,
    collVersion: String,
    collVideo: String,
    host: String,
    port: Int,
    tweetsDb: String
)

case class ServerConfig(host: String, port: Int)

case class SendGridConfig(baseUrl: String, apiKey: String, senderId: Int, listIds: List[String], unsubscribeListId: Int)

case class FreeMarkerConfig(templatesFolderSystemPath: Option[String])
