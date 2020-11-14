import sbt.Keys._
import sbt._

lazy val shared = project
  .in(file("shared"))
  .settings(
    name := "shared",
    CommonSettings,
    libraryDependencies ++= Dependencies.Shared
  )

lazy val tweetsCategorizer = project
  .in(file("tweets-categorizer"))
  .settings(
    name := "tweets-categorizer",
    CommonSettings,
    libraryDependencies ++= Dependencies.TweetsCategorizer
  )
  .dependsOn(shared)

lazy val tweetsUI = project
  .in(file("tweets-ui"))
  .settings(
    name := "tweets-ui",
    CommonSettings,
    libraryDependencies ++= Dependencies.TweetsUI
  )
  .dependsOn(shared)

val CommonSettings: Seq[Def.Setting[_]] = Seq(
  scalaVersion := "2.13.1",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Werror",
//    "-Xlint",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
//    "-Wunused",
    "-Xcheckinit"
  ),
  fork in run := true,
  logBuffered in Test := false
)

lazy val root = project
  .in(file("."))
  .aggregate(
    tweetsCategorizer
  )
  .settings(
    name := "kafka-news-aggregator"
  )