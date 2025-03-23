package ru.oftendev.xbattlepass.commands

import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.util.NumberUtils
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.toNiceString
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import ru.oftendev.xbattlepass.api.giveExactBPExperience
import ru.oftendev.xbattlepass.battlepass.BattlePass
import ru.oftendev.xbattlepass.plugin

object GiveCommand: PluginCommand(
    plugin,
    "give",
    "xbattlepass.command.give",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        val playerString = args.firstOrNull() ?: run {
            sender.sendMessage(plugin.langYml.getMessage("player-required"))
            return
        }

        val player = Bukkit.getPlayer(playerString) ?: run {
            sender.sendMessage(plugin.langYml.getMessage("player-not-found"))
            return
        }

        val amountString = args.getOrNull(1) ?: run {
            sender.sendMessage(plugin.langYml.getMessage("amount-required"))
            return
        }

        val amount = amountString.toDoubleOrNull() ?: run {
            sender.sendMessage(plugin.langYml.getMessage("invalid-amount"))
            return
        }

        player.giveExactBPExperience(amount)

        sender.sendMessage(plugin.langYml.getMessage("given-experience")
            .replace("%playername%", player.name)
            .replace("%amount%", amount.toNiceString())
        )
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when(args.size) {
            1 -> StringUtil.copyPartialMatches(args.first(), Bukkit.getOnlinePlayers().map { it.name }, mutableListOf())
            else -> emptyList()
        }
    }
}