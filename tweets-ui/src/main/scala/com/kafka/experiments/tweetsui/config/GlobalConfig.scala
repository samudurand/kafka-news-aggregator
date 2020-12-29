package com.kafka.experiments.tweetsui.config

case class GlobalConfig(
    freemarker: FreeMarkerConfig,
    mongodb: MongodbConfig,
    keywords: KeywordsConfig,
    sendgrid: SendGridConfig,
    server: ServerConfig
)

case class KeywordsConfig(
    related: Seq[String]
)

case class MongodbConfig(
    host: String,
    port: Int
)

case class ServerConfig(host: String, port: Int)

case class SendGridConfig(baseUrl: String, apiKey: String, senderId: Int, listIds: List[String], unsubscribeListId: Int)

case class FreeMarkerConfig(templatesFolderSystemPath: Option[String])
