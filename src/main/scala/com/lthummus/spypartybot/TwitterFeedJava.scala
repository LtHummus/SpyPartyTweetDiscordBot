package com.lthummus.spypartybot

import java.util.concurrent.Executors

import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken}
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods.parse
import org.slf4j.LoggerFactory
import twitter4j._
import twitter4j.conf.ConfigurationBuilder

import scala.concurrent.{ExecutionContext, Future}

object TwitterFeedJava extends App {

  implicit val Formats = DefaultFormats
  implicit val context = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5))

  val Logger = LoggerFactory.getLogger("TwitterFeed")

  Logger.info("Reading config...")
  val source = scala.io.Source.fromFile("config.json")
  val config = parse(source.reader())
  source.close()

  val twitterConsumerTokenKey = (config \ "twitter-consumer-key").extract[String]
  val twitterConsumerTokenSecret = (config \ "twitter-consumer-secret").extract[String]
  val twitterAccessTokenKey = (config \ "twitter-access-key").extract[String]
  val twitterAccessTokenSecret = (config \ "twitter-access-secret").extract[String]

  val toFollow = (config \ "twitter-id-to-monitor").extract[Long]
  val thumbnailThreshold = (config \ "thumbnail-threshold").extract[Int]

  implicit val discordWebhook: String = (config \ "discord-webhook").extract[String]

  private val TwitterConsumerToken = ConsumerToken(key = twitterConsumerTokenKey, secret = twitterConsumerTokenSecret)
  private val TwitterAccessToken = AccessToken(key = twitterAccessTokenKey, secret = twitterAccessTokenSecret)

  Logger.info("Finished reading configuration")
  Logger.info("Thumbnail threshold: {}", thumbnailThreshold)

  private def extractUrls(tweet: Status): Seq[String] = {
    val entities = tweet.getURLEntities
    entities.map { url =>
      val expandedUrl = url.getExpandedURL
      val timesStreamed = expandedUrl.substring(expandedUrl.lastIndexOf('#') + 1).toInt

      Logger.info(s"Found URL: $expandedUrl which has streamed $timesStreamed")

      if (timesStreamed >= thumbnailThreshold) {
        s"New SpyParty Stream: $expandedUrl"
      } else {
        s"New SpyParty Stream: <$expandedUrl>"
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
      System.exit(2)
    }

    override def onException(ex: Exception): Unit = {
      Logger.error("Exception from stream", ex)
      System.exit(1)
    }
  }

  val auth = new ConfigurationBuilder()
    .setOAuthConsumerKey(twitterConsumerTokenKey)
    .setOAuthConsumerSecret(twitterConsumerTokenSecret)
    .setOAuthAccessToken(twitterAccessTokenKey)
    .setOAuthAccessTokenSecret(twitterAccessTokenSecret)
    .build()


  val followFilter = Array[Long](toFollow)

  val stream = new TwitterStreamFactory(auth).getInstance()
  stream.addListener(listener)

  val query = new FilterQuery()
  query.follow(toFollow)

  stream.filter(query)
}
