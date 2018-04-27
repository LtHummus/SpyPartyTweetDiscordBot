package com.lthummus.spypartybot.scltagging

import com.lthummus.spypartybot.Config
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import net.dv8tion.jda.core.managers.{GuildController, GuildManager}
import net.dv8tion.jda.core.{AccountType, JDABuilder}
import org.slf4j.LoggerFactory

class SclTagger {

  import SclTagger._

  private val listener = new ListenerAdapter {
    override def onMessageReceived(event: MessageReceivedEvent): Unit = {
      if (!event.getAuthor.isBot) { //ignore events from other bots
        val message = event.getMessage

        if (message.getChannel.getIdLong == Config.discordChannelId) {
          if (message.getContentRaw == "!add") {
            //TODO: This is probabl expensive to build every time
            val role = message.getGuild.getRoleById(Config.discordRoleId)

            new GuildController(message.getGuild).addSingleRoleToMember(message.getMember, role).queue()
            message.getChannel.sendMessage("Role granted").queue()

            Logger.info(s"Adding role to ${message.getMember.getEffectiveName}")
          } else if (message.getContentRaw == "!remove") {
            val role = message.getGuild.getRoleById(Config.discordRoleId)
            new GuildController(message.getGuild).removeSingleRoleFromMember(message.getMember, role).queue()
            message.getChannel.sendMessage("Role removed").queue()
            Logger.info(s"Removing role from ${message.getMember.getEffectiveName}")
          }
        }
      }
    }
  }

  private val api = new JDABuilder(AccountType.BOT).setToken(Config.discordBotToken).buildAsync()
  api.addEventListener(listener)

}

object SclTagger {
  private val Logger = LoggerFactory.getLogger(classOf[SclTagger])
}
