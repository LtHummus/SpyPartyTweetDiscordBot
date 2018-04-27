package com.lthummus.spypartybot

import com.typesafe.config.ConfigFactory
import scala.collection.JavaConverters._


object Config {

  private lazy val configTree = ConfigFactory.load()

  def twitterConsumerKey: String = configTree.getString("twitter.consumerkey")
  def twitterConsumerSecret: String = configTree.getString("twitter.consumersecret")
  def twitterAccessKey: String = configTree.getString("twitter.accesskey")
  def twitterAccessSecret: String = configTree.getString("twitter.accesssecret")

  def streamsEnabled: Boolean = configTree.getBoolean("streams.enabled")
  def streamIdToMonitor: Long = configTree.getLong("streams.idtomonitor")
  def streamWebhook: String = configTree.getString("streams.webhook")
  def streamThumbnailThreshold: Int = configTree.getInt("streams.thumbnailthreshold")
  def streamBlacklist: Set[String] = configTree.getStringList("streams.blacklist").asScala.toSet

  def discordEnabled: Boolean = configTree.getBoolean("discord.enabled")
  def discordBotToken: String = configTree.getString("discord.token")
  def discordChannelId: Long = configTree.getLong("discord.channelid")
  def discordGuildId: Long = configTree.getLong("discord.guildid")
  def discordSclRoleId: Long = configTree.getLong("discord.sclroleid")
  def discordLfgRoleId: Long = configTree.getLong("discord.lfgroleid")
  def discordLfgTimeoutMinutes: Int = configTree.getInt("discord.lfgtimeout")

}
