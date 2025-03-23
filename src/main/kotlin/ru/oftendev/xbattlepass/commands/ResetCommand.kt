package ru.oftendev.xbattlepass.commands

import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.util.NumberUtils
import com.willfp.eco.util.StringUtils
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import ru.oftendev.xbattlepass.battlepass.BattlePass
import ru.oftendev.xbattlepass.plugin

object ResetCommand: PluginCommand(
    plugin,
    "reset",
    "xbattlepass.command.reset",
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

        BattlePass.reset(player)

        sender.sendMessage(plugin.langYml.getMessage("reset-player")
            .replace("%playername%", player.name))
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when(args.size) {
            1 -> StringUtil.copyPartialMatches(args.first(), Bukkit.getOnlinePlayers().map { it.name }, mutableListOf())
            else -> emptyList()
        }
    }
}