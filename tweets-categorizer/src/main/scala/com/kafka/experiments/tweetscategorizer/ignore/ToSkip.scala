package com.kafka.experiments.tweetscategorizer.ignore

import com.kafka.experiments.shared.LinkUtils.{containsValidLink, expandUrlAndExtractBase, firstValidLink}
import com.kafka.experiments.shared.{LinkUtils, Tweet}
import com.kafka.experiments.tweetscategorizer.config.GlobalConfig
import com.kafka.experiments.tweetscategorizer.RedisService
import com.typesafe.scalalogging.StrictLogging
import pureconfig.ConfigSource
import pureconfig.generic.auto._

class ToSkip(redisService: RedisService) extends StrictLogging {
  private val config = ConfigSource.default.loadOrThrow[GlobalConfig]

  def shouldBeSkipped(tweet: Tweet): Boolean = {
    if (tweet.Retweet || isNotInEnglish(tweet)) {
      logger.debug(s"Tweet should be be skipped: $tweet")
      true
    } else if (tweet.InReplyToStatusId >= 0) {
      logger.debug(s"Tweet replying to another should be skipped: $tweet")
      true
    } else if (config.dropIfNoLink && !containsValidLink(tweet.URLEntities)) {
      logger.debug(s"Tweet without a valid URL should be be skipped: $tweet")
      true
    } else {
      hasAlreadyKnownUrl(tweet)
    }
  }

  private def hasAlreadyKnownUrl(tweet: Tweet) = {
    firstValidLink(tweet.URLEntities) match {
      case Some(url) =>
        val hasKnownUrl = redisService.exists(expandUrlAndExtractBase(url.ExpandedURL))
        if (hasKnownUrl) logger.info(s"Tweet with already known URL should be skipped: $tweet")
        hasKnownUrl
      case _ => false
    }
  }

  private def isNotInEnglish(tweet: Tweet) = {
    tweet.Lang.isDefined && !tweet.Lang.getOrElse("").toLowerCase.equals("en")
  }

}
