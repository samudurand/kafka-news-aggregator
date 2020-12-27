package com.kafka.experiments.tweetscategorizer

import com.kafka.experiments.tweetscategorizer.config.RedisConfig
import com.redis.RedisClient
import scala.concurrent.duration.DurationInt

trait RedisService {
  def putWithExpire(key: String): Boolean
  def exists(key: String): Boolean
}

object RedisService {

  def apply(config: RedisConfig): RedisService = {
    val client = new RedisClient(config.host, config.port)
    new DefaultRedisService(client)
  }
}

class DefaultRedisService(redisClient: RedisClient) extends RedisService {
  val cachedValue = "cached"

  override def putWithExpire(key: String): Boolean = {
    redisClient.set(key, cachedValue, expire = 1.days)
  }

  override def exists(key: String): Boolean = {
    redisClient.get(key).isDefined
  }
}
