package com.lthummus.spypartybot.streams

import io.lemonlabs.uri.AbsoluteUrl

case class TwitchUrl(fullUrl: String, username: String, timesStreamed: Int) {
  override def toString: String = s"$fullUrl (Username: $username, timesStreamed: $timesStreamed)"
}

object TwitchUrl {
  def apply(fullUrl: String): TwitchUrl = {
    val parsedUrl = AbsoluteUrl.parse(fullUrl)

    val username = parsedUrl.path.toRootless.parts.head
    val timesStreamed = fullUrl.substring(fullUrl.lastIndexOf('#') + 1).toInt

    TwitchUrl(fullUrl, username, timesStreamed)
  }
}