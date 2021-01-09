import sbt.Keys._
import sbt._

lazy val shared = project
  .in(file("shared"))
  .configs(IntegrationTest)
  .settings(
    name := "shared",
    CommonSettings,
    libraryDependencies ++= Dependencies.Shared
  )

lazy val tweetsCategorizer = project
  .in(file("tweets-categorizer"))
  .configs(IntegrationTest)
  .enablePlugins(JacocoItPlugin)
  .settings(
    name := "tweets-categorizer",
    CommonSettings,
    libraryDependencies ++= Dependencies.TweetsCategorizer,
    // Sbt assembly plugin
    assemblyJarName in assembly := "categorizer.jar",
    mainClass in assembly := Some("com.kafka.experiments.tweetscategorizer.Main"),
    test in assembly := {},
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", _ @_*) => MergeStrategy.discard
      case _                           => MergeStrategy.first
    }
  )
  .dependsOn(shared)

lazy val tweetsUI = project
  .in(file("tweets-ui"))
  .enablePlugins(JacocoItPlugin)
  .configs(IntegrationTest)
  .settings(
    name := "tweets-ui",
    CommonSettings,
    libraryDependencies ++= Dependencies.TweetsUI,
    // Sbt assembly plugin
    assemblyJarName in assembly := "tweetsui.jar",
    mainClass in assembly := Some("com.kafka.experiments.tweetsui.Main"),
    test in assembly := {},
    assemblyMergeStrategy in assembly := {
      case PathList("reference.conf")  => MergeStrategy.concat
      case PathList("META-INF", _ @_*) => MergeStrategy.discard
      case _                           => MergeStrategy.first
    }
  )
  .dependsOn(shared)

val CommonSettings: Seq[Def.Setting[_]] = Seq(
  Test / fork := true,
  IntegrationTest / fork := true,
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
