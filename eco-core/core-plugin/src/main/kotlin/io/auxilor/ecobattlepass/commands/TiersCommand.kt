package io.auxilor.ecobattlepass.commands

import io.auxilor.ecobattlepass.battlepass.BattlePasses
import io.auxilor.ecobattlepass.commands.helpers.Messages
import io.auxilor.ecobattlepass.commands.helpers.resolveBattlePass
import io.auxilor.ecobattlepass.gui.BattleTiersGUI
import io.auxilor.ecobattlepass.plugin
import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

object TiersCommand : PluginCommand(
    plugin,
    "tiers",
    "ecobattlepass.command.tier",
    true
) {
    override fun onExecute(sender: Player, args: MutableList<String>) {
        if (args.isEmpty()) {
            Messages.sendTiersUsage(sender)
            return
        }

        val pass = (sender as CommandSender).resolveBattlePass(args.getOrNull(0)) ?: run {
            Messages.sendTiersUsage(sender)
            return
        }

        BattleTiersGUI.createAndOpen(sender, pass)
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when (args.size) {
            1 -> StringUtil.copyPartialMatches(
                args[0],
                BattlePasses.values().map { it.id },
                mutableListOf()
            )
            else -> emptyList()
        }
    }
}