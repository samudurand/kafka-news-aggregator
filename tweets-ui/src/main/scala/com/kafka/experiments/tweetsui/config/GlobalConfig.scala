package com.kafka.experiments.tweetsui.config

case class GlobalConfig(mongodb: MongodbConfig, server: ServerConfig)

case class MongodbConfig(
    host: String,
    port: Int,
    tweetsDb: String,
    collDropped: String,
    collAudio: String,
    collArticle: String,
    collVersion: String,
    collInteresting: String
)

case class ServerConfig(port: Int)
