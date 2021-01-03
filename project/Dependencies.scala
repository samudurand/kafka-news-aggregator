import sbt._

object Versions {
  val ApacheHttp = "4.5.13"
  val Circe = "0.13.0"
  val FreeMarker = "2.3.30"
  val Http4s = "0.21.8"
  val KafkaStreams = "2.7.0"
  val Logback = "1.2.3"
  val LogstashEncoder = "6.5"
  val MinifyHtml = "0.3.9"
  val MongoDB = "4.1.1"
  val PureConfig = "0.14.0"
  val Redis = "3.30"
  val ScalaLogging = "3.9.2"
  val Scalamock = "5.1.0"
  val Scalatest = "3.2.2"
  val TestContainers = "0.38.8"
  val Twitter4S = "7.0"
  val UrlDetector = "0.1.23"
  val Wiremock = "2.27.2"
}

object Dependencies {
  val ApacheHttp = "org.apache.httpcomponents" % "httpclient" % Versions.ApacheHttp
  val CirceGeneric = "io.circe" %% "circe-generic" % Versions.Circe
  val CirceParser = "io.circe" %% "circe-parser" % Versions.Circe
  val FreeMarker = "org.freemarker" % "freemarker" % Versions.FreeMarker
  val Http4sCirce = "org.http4s" %% "http4s-circe" % Versions.Http4s
  val Http4sDsl = "org.http4s" %% "http4s-dsl" % Versions.Http4s
  val Http4sClient = "org.http4s" %% "http4s-blaze-client" % Versions.Http4s
  val Http4sServer = "org.http4s" %% "http4s-blaze-server" % Versions.Http4s
  val KafkaStreams = "org.apache.kafka" %% "kafka-streams-scala" % Versions.KafkaStreams
  val Logback = "ch.qos.logback" % "logback-classic" % Versions.Logback
  val LogstashLogbackEncoder = "net.logstash.logback" % "logstash-logback-encoder" % Versions.LogstashEncoder
  val MongoDB = "org.mongodb.scala" %% "mongo-scala-driver" % Versions.MongoDB
  val PureConfig = "com.github.pureconfig" %% "pureconfig" % Versions.PureConfig
  val Redis ="net.debasishg" %% "redisclient" % Versions.Redis
  val ScalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % Versions.ScalaLogging
  val Twitter4S = "com.danielasfregola" %% "twitter4s" % Versions.Twitter4S
  val UrlDetector = "io.github.url-detector" % "url-detector" % Versions.UrlDetector

  val KafkaStreamTest = "org.apache.kafka" % "kafka-streams-test-utils" % Versions.KafkaStreams % "test"
  val Scalatest = "org.scalatest" %% "scalatest" % Versions.Scalatest % "it,test"
  val Scalamock = "org.scalamock" %% "scalamock" % Versions.Scalamock % "test"
  val TestContainers = "com.dimafeng" %% "testcontainers-scala-scalatest" % Versions.TestContainers % "it"
  val TestContainersMongo = "com.dimafeng" %% "testcontainers-scala-mongodb" % Versions.TestContainers % "it"
  val Wiremock = "com.github.tomakehurst" % "wiremock-jre8" % Versions.Wiremock % "it"

  val Shared: Seq[ModuleID] = Seq(
    ApacheHttp,
    CirceGeneric,
    CirceParser,
    Logback,
    LogstashLogbackEncoder,
    ScalaLogging,
    UrlDetector
  )

  val TweetsCategorizer: Seq[ModuleID] = Seq(
    KafkaStreams,
    KafkaStreamTest,
    PureConfig,
    Redis,
    Scalamock,
    Scalatest
  )

  val TweetsUI: Seq[ModuleID] = Seq(
    FreeMarker,
    Http4sCirce,
    Http4sDsl,
    Http4sClient,
    Http4sServer,
    MongoDB,
    PureConfig,
    Scalamock,
    Scalatest,
    TestContainers,
    TestContainersMongo,
    Twitter4S,
    Wiremock
  )
}
