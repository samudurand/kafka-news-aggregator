package com.kafka.experiments.tweetscategorizer.utils

import com.kafka.experiments.shared.User

object UserUtils {

  def userNameContainsAnyOf(user: User, keywords: Seq[String]): Boolean = {
    keywords.exists(user.ScreenName.contains)
  }

}
