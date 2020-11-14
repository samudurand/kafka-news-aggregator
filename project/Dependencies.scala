import sbt._

object Versions {
  val KafkaStreams = "2.5.1"
  val Circe = "0.13.0"
  val Http4s = "0.21.8"
  val Logback = "1.2.3"
  val MongoDB = "4.1.1"
  val PureConfig = "0.14.0"
  val ScalaLogging = "3.9.2"
  val Scalatest = "3.2.2"
}

object Dependencies {
  val CirceGeneric = "io.circe" %% "circe-generic" % Versions.Circe
  val CirceParser = "io.circe" %% "circe-parser" % Versions.Circe
  val Http4sCirce = "org.http4s" %% "http4s-circe" % Versions.Http4s
  val Http4sDsl = "org.http4s" %% "http4s-dsl" % Versions.Http4s
  val Http4sServer = "org.http4s" %% "http4s-blaze-server" % Versions.Http4s
  val KafkaStreams = "org.apache.kafka" %% "kafka-streams-scala" % Versions.KafkaStreams
  val Logback = "ch.qos.logback" % "logback-classic" % Versions.Logback
  val MongoDB = "org.mongodb.scala" %% "mongo-scala-driver" % Versions.MongoDB
  val PureConfig = "com.github.pureconfig" %% "pureconfig" % Versions.PureConfig
  val ScalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % Versions.ScalaLogging
  val Scalatest = "org.scalatest" %% "scalatest" % Versions.Scalatest % "test"

  val Shared: Seq[ModuleID] = Seq(
    CirceGeneric
  )

  val TweetsCategorizer: Seq[ModuleID] = Seq(
    CirceGeneric,
    CirceParser,
    KafkaStreams,
    Logback,
    PureConfig,
    ScalaLogging,
    Scalatest
  )

  val TweetsUI: Seq[ModuleID] = Seq(
    CirceGeneric,
    CirceParser,
    Http4sCirce,
    Http4sDsl,
    Http4sServer,
    Logback,
    MongoDB,
    PureConfig,
    ScalaLogging,
    Scalatest
  )
}
