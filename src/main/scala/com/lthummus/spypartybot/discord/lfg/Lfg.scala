package com.lthummus.spypartybot.discord.lfg

import java.util.concurrent.{Executors, TimeUnit}

import com.lthummus.spypartybot.BotConfig
import net.dv8tion.jda.core.entities.{Member, Message, User}
import net.dv8tion.jda.core.managers.{GuildController, GuildManager}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

class Lfg {

  import Lfg.Logger

  private val executor = Executors.newScheduledThreadPool(3)
  private val lfgRoleId = BotConfig.discordLfgRoleId
  private val lfgTimeoutMinutes = BotConfig.discordLfgTimeoutMinutes

  private def hasLfgRole(member: Member): Boolean = {
    member.getRoles.asScala.count(_.getIdLong == lfgRoleId) > 0
  }

  def addLfg(message: Message): Unit = {
    val role = message.getGuild.getRoleById(lfgRoleId)
    val gc = new GuildController(message.getGuild)
    if (!hasLfgRole(message.getMember)) {
      gc.addSingleRoleToMember(message.getMember, role).queue()
      message.getChannel.sendMessage("Role granted").queue()

      executor.schedule[Unit](() => {
        message.getAuthor.openPrivateChannel().queue(channel => {
          //XXX: this is done to refresh the roles from the server or else we use
          //     the cached ones when the message was sent (useless!). Is there a
          //     better way? Also this needs Manage Server permissions
          val user = new GuildManager(message.getGuild).getGuild.getMember(message.getAuthor)
          if (hasLfgRole(user)) {
            channel.sendMessage(s"$lfgTimeoutMinutes minutes are up. You are no longer LFG").queue()
          }
        })
        gc.removeSingleRoleFromMember(message.getMember, role).queue()
      }, lfgTimeoutMinutes, TimeUnit.MINUTES)

      Logger.info(s"Adding role to ${message.getMember.getEffectiveName}")
    }
  }

  def removeLfg(message: Message): Unit = {
    if (hasLfgRole(message.getMember)) {
      val role = message.getGuild.getRoleById(lfgRoleId)
      new GuildController(message.getGuild).removeSingleRoleFromMember(message.getMember, role).queue()
      message.getChannel.sendMessage("Role removed").queue()

      Logger.info(s"Removing role from ${message.getMember.getEffectiveName}")
    }
  }
}

object Lfg {
  private val Logger = LoggerFactory.getLogger(classOf[Lfg])
}
