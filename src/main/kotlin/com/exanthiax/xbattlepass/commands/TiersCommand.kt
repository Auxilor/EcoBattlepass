package com.exanthiax.xbattlepass.commands

import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import com.exanthiax.xbattlepass.battlepass.BattlePasses
import com.exanthiax.xbattlepass.gui.BattleTiersGUI
import com.exanthiax.xbattlepass.gui.CategoriesGUI
import com.exanthiax.xbattlepass.plugin

object TiersCommand: PluginCommand(
    plugin,
    "tier",
    "xbattlepass.command.tier",
    true
) {
    override fun onExecute(sender: Player, args: MutableList<String>) {
        val passString = args.getOrNull(0) ?: run {
            sender.sendMessage(plugin.langYml.getMessage("pass-required"))
            return
        }

        val pass = BattlePasses.getByID(passString) ?: run {
            sender.sendMessage(plugin.langYml.getMessage("pass-not-found"))
            return
        }

        BattleTiersGUI.createAndOpen(sender, pass)
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when(args.size) {
            1 -> StringUtil.copyPartialMatches(args[0], BattlePasses.values().map { it.id }, mutableListOf())
            else -> emptyList()
        }
    }
}