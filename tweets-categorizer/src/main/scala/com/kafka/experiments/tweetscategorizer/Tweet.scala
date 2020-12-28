package com.kafka.experiments.tweetscategorizer

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

object Tweet {
  implicit val codec: Codec[Tweet] = deriveCodec
}

case class Tweet(
    CreatedAt: Long,
    Id: Long,
    Text: String,
    FavoriteCount: Long,
    Retweet: Boolean,
    RetweetCount: Long,
    Lang: Option[String],
    URLEntities: List[URLEntity],
    UserMentionEntities: List[User],
    User: User
)

object URLEntity {
  implicit val codec: Codec[URLEntity] = deriveCodec
}
case class URLEntity(URL: String, ExpandedURL: String)

object User {
  implicit val codec: Codec[User] = deriveCodec
}
case class User(Id: Long, ScreenName: String)
