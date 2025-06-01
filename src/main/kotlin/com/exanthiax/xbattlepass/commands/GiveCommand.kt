package com.exanthiax.xbattlepass.commands

import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.util.toNiceString
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import com.exanthiax.xbattlepass.api.giveExactBPExperience
import com.exanthiax.xbattlepass.api.giveExactBPTiers
import com.exanthiax.xbattlepass.battlepass.BattlePasses
import com.exanthiax.xbattlepass.plugin

object GiveCommand: PluginCommand(
    plugin,
    "give",
    "xbattlepass.command.give",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        val playerString = args.getOrNull(0) ?: run {
            sender.sendMessage(plugin.langYml.getMessage("player-required"))
            return
        }

        val players = if (playerString.equals("all", ignoreCase = true)) {
            Bukkit.getOnlinePlayers().toList()
        } else {
            val player = Bukkit.getPlayer(playerString)
            if (player == null) {
                sender.sendMessage(plugin.langYml.getMessage("player-not-found"))
                return
            }
            listOf(player)
        }

        val passString = args.getOrNull(1) ?: run {
            sender.sendMessage(plugin.langYml.getMessage("pass-required"))
            return
        }

        val pass = BattlePasses.getByID(passString) ?: run {
            sender.sendMessage(plugin.langYml.getMessage("pass-not-found"))
            return
        }

        val mode = args.getOrNull(2)?.lowercase() ?: run {
            sender.sendMessage(plugin.langYml.getMessage("type-required"))
            return
        }

        val amountString = args.getOrNull(3) ?: run {
            sender.sendMessage(plugin.langYml.getMessage("amount-required"))
            return
        }

        val amount = amountString.toDoubleOrNull() ?: run {
            sender.sendMessage(plugin.langYml.getMessage("invalid-amount"))
            return
        }

        for (player in players) {
            when (mode) {
                "xp", "experience" -> player.giveExactBPExperience(pass, amount)
                "tier", "tiers" -> player.giveExactBPTiers(pass, amount.toInt())
                else -> {
                    sender.sendMessage(plugin.langYml.getMessage("invalid-type"))
                    return
                }
            }

            val messageKeySender = if (mode == "xp") "given-experience" else "given-tiers"
            sender.sendMessage(plugin.langYml.getMessage(messageKeySender)
                .replace("%playername%", player.name)
                .replace("%amount%", amount.toNiceString())
                .replace("%pass%", pass.name)
            )

            val messageKeyReceiver = if (mode == "xp") "received-experience" else "received-tiers"
            player.sendMessage(plugin.langYml.getMessage(messageKeyReceiver)
                .replace("%amount%", amount.toNiceString())
                .replace("%pass%", pass.name)
            )
        }
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when(args.size) {
            1 -> StringUtil.copyPartialMatches(args[0], Bukkit.getOnlinePlayers().map { it.name } + "all", mutableListOf())
            2 -> StringUtil.copyPartialMatches(args[1], BattlePasses.values().map { it.id }, mutableListOf())
            3 -> StringUtil.copyPartialMatches(args[2], listOf("xp", "tier"), mutableListOf())
            4 -> StringUtil.copyPartialMatches(args[3], listOf("1", "10", "100", "1000"), mutableListOf())
            else -> emptyList()
        }
    }
}
