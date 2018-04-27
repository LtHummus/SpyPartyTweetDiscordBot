package com.lthummus.spypartybot

import com.lthummus.spypartybot.scltagging.SclTagger
import com.lthummus.spypartybot.streams.TwitterFeedJava
import org.slf4j.LoggerFactory
import org.slf4j.impl.SimpleLogger

object Main extends App {


  private val Logger = LoggerFactory.getLogger("Main")

  Logger.info("Hello world!")

  //start tweet stream
  if (Config.streamsEnabled) {
    new Thread(() => {
      while (true) {
        Logger.info("Starting tweet thread")
        try {
          new TwitterFeedJava()
        } catch {
          case e: Throwable => Logger.warn("Got error from twitter thread. Restarting", e)
        }
        Logger.warn("Twitter thread died. Restarting")
        Thread.sleep(1000)
      }
    }).start()
  }

  if (Config.discordEnabled) {
    new Thread(() => {
      Logger.info("Starting Discord ROLE manager")
      try {
        new SclTagger()
      } catch {
        case e: Throwable => Logger.warn("Got error from discord thread. Restarting", e)
      }

      Thread.sleep(1000)
    }).start()
  }


}
