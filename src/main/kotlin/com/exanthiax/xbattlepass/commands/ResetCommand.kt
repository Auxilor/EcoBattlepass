package com.exanthiax.xbattlepass.commands

import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import com.exanthiax.xbattlepass.battlepass.BattlePasses
import com.exanthiax.xbattlepass.plugin

object ResetCommand : PluginCommand(
    plugin,
    "reset",
    "xbattlepass.command.reset",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        val playerArg = args.getOrNull(0) ?: run {
            sender.sendMessage(plugin.langYml.getMessage("player-required"))
            return
        }

        val passArg = args.getOrNull(1) ?: run {
            sender.sendMessage(plugin.langYml.getMessage("pass-required"))
            return
        }

        val pass = BattlePasses.getByID(passArg) ?: run {
            sender.sendMessage(plugin.langYml.getMessage("pass-not-found"))
            return
        }

        val isAllPlayers = playerArg.equals("all", ignoreCase = true)

        val players = if (isAllPlayers) {
            Bukkit.getOnlinePlayers().toList()
        } else {
            val player = Bukkit.getPlayer(playerArg) ?: run {
                sender.sendMessage(plugin.langYml.getMessage("player-not-found"))
                return
            }
            listOf(player)
        }

        for (player in players) {
            pass.reset(player)
        }

        sender.sendMessage(
            plugin.langYml.getMessage("reset-player")
                .replace("%playername%", if (isAllPlayers) "all" else players.first().name)
                .replace("%pass%", pass.name)
        )
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when (args.size) {
            1 -> StringUtil.copyPartialMatches(
                args[0],
                listOf("all") + Bukkit.getOnlinePlayers().map { it.name },
                mutableListOf()
            )
            2 -> StringUtil.copyPartialMatches(
                args[1],
                BattlePasses.values().map { it.id },
                mutableListOf()
            )
            else -> emptyList()
        }
    }
}
