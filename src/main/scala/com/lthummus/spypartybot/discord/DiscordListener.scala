package com.lthummus.spypartybot.discord

import com.lthummus.spypartybot.Config
import com.lthummus.spypartybot.discord.lfg.Lfg
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import net.dv8tion.jda.core.{AccountType, JDABuilder}

class DiscordListener {

  private val channelId = Config.discordChannelId
  private val lfg = new Lfg()
  private val helpText =
    """
      |Bot commands:
      |`!scladd` -- give yourself the SCL role
      |`!sclremove` -- remove the SCL role from yourself
      |`!lfg` -- tag yourself as looking for game (expires after 15 minutes)
      |`!nolfg` -- untag the LFG role
      |`!help` -- prints this help
    """.stripMargin

  private def printHelp(message: Message): Unit = {
    message.getChannel.sendMessage(helpText).queue()
  }

  private val listener = new ListenerAdapter {
    override def onMessageReceived(event: MessageReceivedEvent): Unit = {
      if (!event.getAuthor.isBot && event.getMessage.getChannel.getIdLong == channelId) { //ignore events from other bots
        event.getMessage.getContentRaw match {
          case "!scladd"    => SclRole.sclAdd(event.getMessage)
          case "!sclremove" => SclRole.sclRemove(event.getMessage)
          case "!lfg"       => lfg.addLfg(event.getMessage)
          case "!nolfg"     => lfg.removeLfg(event.getMessage)
          case "!help"      => printHelp(event.getMessage)
          case _            => event.getMessage.delete().queue()
        }
      }
    }
  }

  private val api = new JDABuilder(AccountType.BOT).setToken(Config.discordBotToken).buildAsync()
  api.addEventListener(listener)

}

