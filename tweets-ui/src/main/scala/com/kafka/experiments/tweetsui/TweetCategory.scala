package com.kafka.experiments.tweetsui

object TweetCategory {

  def allInterestingCategories(): Seq[TweetCategory] =
    List(Article, Audio, Other, VersionRelease, Video)

  def fromName(name: String): Option[TweetCategory] =
    allCategories().find(_.name == name)

  def allCategories(): Seq[TweetCategory] =
    List(Article, Audio, Other, VersionRelease, Video, Excluded)
}

sealed abstract class TweetCategory(val name: String) {}
case object Article extends TweetCategory("article")
case object Audio extends TweetCategory("audio")
case object Other extends TweetCategory("other")
case object VersionRelease extends TweetCategory("version")
case object Video extends TweetCategory("video")
case object Excluded extends TweetCategory("excluded")
