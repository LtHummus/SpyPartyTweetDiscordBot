package com.lthummus.spypartybot.streams

import java.util.concurrent.Executors

import com.lthummus.spypartybot.{BotConfig, LinkCache}
import org.slf4j.LoggerFactory
import twitter4j._
import twitter4j.conf.ConfigurationBuilder

import scala.concurrent.{ExecutionContext, Future}

class TwitterFeedJava {

  implicit val context: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5))

  private val Logger = LoggerFactory.getLogger("TwitterFeed")


  private val twitterConsumerTokenKey = BotConfig.twitterConsumerKey
  private val twitterConsumerTokenSecret = BotConfig.twitterConsumerSecret
  private val twitterAccessTokenKey = BotConfig.twitterAccessKey
  private val twitterAccessTokenSecret = BotConfig.twitterAccessSecret

  private val toFollow = BotConfig.streamIdToMonitor
  private val thumbnailThreshold = BotConfig.streamThumbnailThreshold

  private val expansionBlacklist = BotConfig.streamBlacklist
  private val suppressList = BotConfig.streamSuppress

  Logger.info(s"Expansion blacklist contains ${expansionBlacklist.size} elements")
  expansionBlacklist.foreach(x => Logger.info(s"Banned from expansion $x"))

  Logger.info(s"Suppression list contains ${suppressList.size} elements")
  suppressList.foreach(x => Logger.info(s"Suppressing $x"))

  implicit val discordWebhook: String = BotConfig.streamWebhook

  private val cache = new LinkCache()

  private val $LOCK = new Object()

  Logger.info("Finished reading configuration")
  Logger.info("Thumbnail threshold: {}", thumbnailThreshold)

  private def extractUrls(tweet: Status): Seq[String] = {
    val entities = tweet.getURLEntities
    entities.flatMap { url =>
      val parsedUrl = TwitchUrl(url.getExpandedURL)

      if (cache.isInCache(parsedUrl.fullUrl)) {
        Logger.warn(s"Found duplicate url $parsedUrl")
        None
      } else {

        Logger.info(s"Found URL: $parsedUrl")
        cache.insert(parsedUrl.fullUrl)

        if (suppressList.contains(parsedUrl.username)) {
          Logger.info(s"Skipping ${parsedUrl.username} since it's suppressed")
          None
        } else if (parsedUrl.timesStreamed >= thumbnailThreshold && !expansionBlacklist.contains(parsedUrl.username)) {
          Some(s"New SpyParty Stream: ${parsedUrl.fullUrl}")
        } else {
          Some(s"New SpyParty Stream: <${parsedUrl.fullUrl}>")
        }
      }
    }
  }

  val listener = new StatusListener {

    override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {}

    override def onScrubGeo(userId: Long, upToStatusId: Long): Unit = {}

    override def onStatus(status: Status): Unit = {
      Logger.info("Found tweet")
      Future {
        val texts = extractUrls(status)
        texts.foreach( text => {
          val code = PostToDiscord.postToDiscord(text)
          if (code != 200 && code != 204) {
            Logger.warn("Did not post to discord...error code {}", code)
          }
        })
      }


    }

    override def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = {}

    override def onStallWarning(warning: StallWarning): Unit = {
      Logger.error("Stall warning")
      System.exit(1)
      $LOCK.notify()
    }

    override def onException(ex: Exception): Unit = {
      Logger.error("Exception from stream", ex)
      System.exit(1)
      $LOCK.notify()
    }
  }

  private val auth = new ConfigurationBuilder()
    .setOAuthConsumerKey(twitterConsumerTokenKey)
    .setOAuthConsumerSecret(twitterConsumerTokenSecret)
    .setOAuthAccessToken(twitterAccessTokenKey)
    .setOAuthAccessTokenSecret(twitterAccessTokenSecret)
    .build()


  private val stream = new TwitterStreamFactory(auth).getInstance()
  stream.addListener(listener)


  def go(): Unit = {
    val query = new FilterQuery()
    query.follow(toFollow)

    stream.filter(query)

    try {
      $LOCK.synchronized(
        $LOCK.wait()
      )
    } catch {
      case _: InterruptedException => //nop
    }

    stream.shutdown()
    throw new IllegalMonitorStateException("Thread died :(")
  }


}
