package com.lthummus.spypartybot.streams

import java.util.concurrent.Executors

import com.lthummus.spypartybot.{Config, LinkCache}
import org.slf4j.LoggerFactory
import twitter4j._
import twitter4j.conf.ConfigurationBuilder

import scala.concurrent.{ExecutionContext, Future}

class TwitterFeedJava {

  implicit val context: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5))

  private val Logger = LoggerFactory.getLogger("TwitterFeed")


  private val twitterConsumerTokenKey = Config.twitterConsumerKey
  private val twitterConsumerTokenSecret = Config.twitterConsumerSecret
  private val twitterAccessTokenKey = Config.twitterAccessKey
  private val twitterAccessTokenSecret = Config.twitterAccessSecret

  private val toFollow = Config.streamIdToMonitor
  private val thumbnailThreshold = Config.streamThumbnailThreshold

  private val expansionBlacklist = Config.streamBlacklist

  Logger.info(s"Expansion blacklist contains ${expansionBlacklist.size} elements")
  expansionBlacklist.foreach(x => Logger.info(s"Banned $x"))

  implicit val discordWebhook: String = Config.streamWebhook

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

        if (parsedUrl.timesStreamed >= thumbnailThreshold && !expansionBlacklist.contains(parsedUrl.username)) {
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
      $LOCK.notify()
    }

    override def onException(ex: Exception): Unit = {
      Logger.error("Exception from stream", ex)
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

}
