package com.lthummus.spypartybot

import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken, Tweet}
import com.danielasfregola.twitter4s.entities.streaming.UserStreamingMessage
import org.json4s._
import org.json4s.native.JsonMethods._
import org.slf4j.LoggerFactory


object TwitterFeed extends App {
  implicit val Formats = DefaultFormats

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


  private def extractUrl(tweet: Tweet): Unit = {
    tweet.entities match {
      case None => //nop
      case Some(entityList) if entityList.urls.nonEmpty =>
        val expandedUrl = entityList.urls.head.expanded_url
        val message = s"New SpyParty Stream: $expandedUrl"
        PostToDiscord.postToDiscord(message)
        Logger.info("Posted '{}'", message)
    }
  }

  def post: PartialFunction[UserStreamingMessage, Unit] = {
    case tweet: Tweet => extractUrl(tweet)
  }

  val client = TwitterStreamingClient(TwitterConsumerToken, TwitterAccessToken)
  client.filterStatuses(follow = Seq(toFollow))(post)

}
