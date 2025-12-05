package com.exanthiax.xbattlepass.commands

import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import com.exanthiax.xbattlepass.api.hasPremium
import com.exanthiax.xbattlepass.api.setPremium
import com.exanthiax.xbattlepass.battlepass.BattlePasses
import com.exanthiax.xbattlepass.plugin
import com.willfp.eco.util.formatEco

object SetPremiumCommand : PluginCommand(
    plugin,
    "setpremium",
    "xbattlepass.command.setpass",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        val playerName = args.getOrNull(0) ?: run {
            sender.sendMessage(plugin.langYml.getMessage("player-required"))
            return
        }

        val player = Bukkit.getPlayer(playerName) ?: run {
            sender.sendMessage(plugin.langYml.getMessage("player-not-found"))
            return
        }

        val passId = args.getOrNull(1) ?: run {
            sender.sendMessage(plugin.langYml.getMessage("pass-required"))
            return
        }

        val pass = BattlePasses.getByID(passId) ?: run {
            sender.sendMessage(plugin.langYml.getMessage("pass-not-found"))
            return
        }

        val arg3 = args.getOrNull(2)?.lowercase()
        val arg4 = args.getOrNull(3)?.lowercase()

        val setPremium = when {
            arg3 == null -> true
            arg3 == "silent" -> true
            else -> arg3 !in listOf("false", "no", "0")
        }

        val silent = arg4 == "silent" || (arg3 == "silent" && arg4 == null)

        val hasPremium = player.hasPremium(pass)
        if (setPremium && hasPremium) {
            sender.sendMessage(
                plugin.langYml.getMessage("already-premium")
                    .replace("%playername%", player.name)
                    .replace("%pass%", pass.name)
            )
            return
        } else if (!setPremium && !hasPremium) {
            sender.sendMessage(
                plugin.langYml.getMessage("not-premium")
                    .replace("%playername%", player.name)
                    .replace("%pass%", pass.name)
            )
            return
        }

        player.setPremium(pass, setPremium)

        val messageKey = if (setPremium) "premium-given" else "premium-removed"
        sender.sendMessage(
            plugin.langYml.getMessage(messageKey)
                .replace("%playername%", player.name)
                .replace("%pass%", pass.name)
        )

        val playerMessageKey = if (setPremium) "premium-unlocked" else "premium-revoked"
        player.sendMessage(
            plugin.langYml.getMessage(playerMessageKey)
                .replace("%pass%", pass.name)
        )

        if (setPremium && !silent) {
            Bukkit.broadcastMessage(
                plugin.langYml.getMessage("premium-broadcast")
                    .replace("%playername%", player.name)
                    .replace("%pass%", pass.name)
                    .formatEco(player)
            )
        }
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when (args.size) {
            1 -> StringUtil.copyPartialMatches(
                args[0],
                Bukkit.getOnlinePlayers().map { it.name },
                ArrayList()
            )
            2 -> StringUtil.copyPartialMatches(
                args[1],
                BattlePasses.values().map { it.id },
                ArrayList()
            )
            3 -> StringUtil.copyPartialMatches(
                args[2],
                listOf("true", "false"),
                ArrayList()
            )
            4 -> StringUtil.copyPartialMatches(
                args[3],
                listOf("silent"),
                ArrayList()
            )
            else -> emptyList()
        }
    }
}
