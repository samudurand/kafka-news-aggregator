package com.kafka.experiments.tweetsui

object TweetCategory {

  def allInterestingCategories(): Seq[TweetCategory] =
    List(Article, Audio, Interesting, VersionRelease, Video)

  def allCategories(): Seq[TweetCategory] =
    List(Article, Audio, Interesting, VersionRelease, Video, Excluded)

  def fromName(name: String): Option[TweetCategory] =
    allCategories().find(_.name == name)
}

sealed abstract class TweetCategory(val name: String) {}
case object Article extends TweetCategory("article")
case object Audio extends TweetCategory("audio")
case object Interesting extends TweetCategory("interesting")
case object VersionRelease extends TweetCategory("version")
case object Video extends TweetCategory("video")
case object Excluded extends TweetCategory("excluded")
