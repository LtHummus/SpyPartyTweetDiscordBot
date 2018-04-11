package com.lthummus.spypartybot

import java.util.concurrent.Executors

import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken, Tweet}
import com.danielasfregola.twitter4s.entities.streaming.UserStreamingMessage
import com.danielasfregola.twitter4s.entities.streaming.common.{DisconnectMessage, WarningMessage}
import org.json4s._
import org.json4s.native.JsonMethods._
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}


object TwitterFeed extends App {
  implicit val Formats = DefaultFormats
  //doing this in a single thread for now...is it a good idea? who knows!?
  implicit val context = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

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

  implicit val discordWebhook: String = (config \ "discord-webhook").extract[String]

  private val TwitterConsumerToken = ConsumerToken(key = twitterConsumerTokenKey, secret = twitterConsumerTokenSecret)
  private val TwitterAccessToken = AccessToken(key = twitterAccessTokenKey, secret = twitterAccessTokenSecret)

  Logger.info("Finished reading configuration")

  private def extractUrls(tweet: Tweet): Seq[String] = {
    tweet.entities match {
      case Some(entityList) if entityList.urls.nonEmpty =>
        entityList.urls.map(details => {
          val expandedUrl = details.expanded_url
          Logger.info(s"Found URL: $expandedUrl")
          s"New SpyParty Stream: <$expandedUrl>"
        })
      case _ => Seq()
    }
  }

  def post: PartialFunction[UserStreamingMessage, Unit] = {
    case tweet: Tweet =>
      Logger.info("Found tweet")
      Future {
        val texts = extractUrls(tweet)
        texts.foreach(text => {
          val code = PostToDiscord.postToDiscord(text)
          if (code != 200 && code != 204) {
            Logger.warn("Did not post to discord...error code {}", code)
          }
        })
      }
    case message: WarningMessage =>
      Logger.warn(message.toString)
    case disconnect: DisconnectMessage =>
      Logger.error("Disconnected from stream. Exiting. {}", disconnect)
      System.exit(1)

  }

  val client = TwitterStreamingClient(TwitterConsumerToken, TwitterAccessToken)
  Logger.info("Connecting to twitter stream")
  client.filterStatuses(stall_warnings = true, follow = Seq(toFollow))(post)





}
