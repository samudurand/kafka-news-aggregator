package com.kafka.experiments.tweetsui

import cats.effect.{ContextShift, IO}
import com.dimafeng.testcontainers.{FixedHostPortGenericContainer, ForEachTestContainer}
import com.kafka.experiments.tweetsui.client.MongoService
import com.kafka.experiments.tweetsui.config.MongodbConfig
import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent.ExecutionContext.global

trait MongoDatabase extends ForEachTestContainer {
  this: AnyFlatSpec =>

  implicit val contextShift: ContextShift[IO] = IO.contextShift(global)

  override val container = new FixedHostPortGenericContainer(
    "mongo:4.4.2",
    exposedContainerPort = 27017,
    exposedHostPort = 28017
  )

  protected val mongoService: MongoService = MongoService(MongodbConfig("localhost", 28017))
}
