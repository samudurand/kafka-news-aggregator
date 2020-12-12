package com.kafka.experiments.tweetsui.config

case class GlobalConfig(mongodb: MongodbConfig, server: ServerConfig)

case class MongodbConfig(
    collArticle: String,
    collAudio: String,
    collExcluded: String,
    collExaminate: String,
    collInteresting: String,
    collPromotion: String,
    collVersion: String,
    collVideo: String,
    host: String,
    port: Int,
    tweetsDb: String
)

case class ServerConfig(host: String, port: Int)
