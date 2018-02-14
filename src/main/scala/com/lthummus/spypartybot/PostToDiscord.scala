package com.lthummus.spypartybot

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._

import scalaj.http.Http

object PostToDiscord {

  private def makePayload(msg: String): String = {
    val data = "content" -> msg
    compact(render(data))
  }

  def postToDiscord(msg: String)(implicit webhookUrl: String): Int = {
    Http(webhookUrl)
      .postData(makePayload(msg))
      .asString
      .code
  }

}
