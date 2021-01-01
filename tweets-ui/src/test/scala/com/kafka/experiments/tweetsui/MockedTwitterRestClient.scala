package com.kafka.experiments.tweetsui

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{RatedData, Tweet}

import scala.concurrent.{ExecutionContext, Future}

class MockedTwitterRestClient(metadata: Seq[Tweet])(implicit ex: ExecutionContext)
    extends TwitterRestClient(consumerToken = null, accessToken = null) {

  override def tweetLookup(ids: Long*): Future[RatedData[Seq[Tweet]]] = {
    Future(RatedData(null, metadata))
  }
}
