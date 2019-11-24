package com.lthummus.spypartybot.streams

import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{compact, render}
import org.slf4j.LoggerFactory
import scalaj.http.Http

object PostToDiscord {

  val Logger = LoggerFactory.getLogger("PostToDiscord")


  private def makePayload(msg: String): String = {
    val data = "content" -> msg
    compact(render(data))
  }

  def postToDiscord(msg: String)(implicit webhookUrl: String): Int = {
    Logger.info("Posting \"{}\" to Discord", msg)
    val payload = makePayload(msg)
    Logger.info(s"Payload = `$payload`")

    val res = Http(webhookUrl)
      .postData(payload)
      .header("content-type", "application/json")
      .asString

    Logger.info(s"Response body: `${res.body}`")
    res.code

  }
}

