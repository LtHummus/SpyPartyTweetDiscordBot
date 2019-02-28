package com.lthummus.spypartybot.discord

import com.lthummus.spypartybot.BotConfig
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.managers.GuildController
import org.slf4j.LoggerFactory

object SclRole {
  private val Logger = LoggerFactory.getLogger("SclRole")


  def sclAdd(message: Message): Unit = {
    //TODO: This is probabl expensive to build every time
    val role = message.getGuild.getRoleById(BotConfig.discordSclRoleId)

    new GuildController(message.getGuild).addSingleRoleToMember(message.getMember, role).queue()
    message.getChannel.sendMessage("Role granted").queue()

    Logger.info(s"Adding role to ${message.getMember.getEffectiveName}")
  }

  def sclRemove(message: Message): Unit = {
    val role = message.getGuild.getRoleById(BotConfig.discordSclRoleId)
    new GuildController(message.getGuild).removeSingleRoleFromMember(message.getMember, role).queue()
    message.getChannel.sendMessage("Role removed").queue()
    Logger.info(s"Removing role from ${message.getMember.getEffectiveName}")
  }
}
