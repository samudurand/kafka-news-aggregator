package com.kafka.experiments.tweetsui.config

case class GlobalConfig(mongodb: MongodbConfig, server: ServerConfig)

case class MongodbConfig(host: String, port: Int, tweetsDb: String, collDropped: String, collInteresting: String)

case class ServerConfig(port: Int)
