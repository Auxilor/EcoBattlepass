package com.exanthiax.ecobattlepass.commands

import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.exanthiax.ecobattlepass.commands.helpers.Messages
import com.exanthiax.ecobattlepass.commands.helpers.resolveBattlePass
import com.exanthiax.ecobattlepass.gui.CategoriesGUI
import com.exanthiax.ecobattlepass.gui.QuestsGUI
import com.exanthiax.ecobattlepass.plugin
import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

object QuestsCommand : PluginCommand(
    plugin,
    "quests",
    "ecobattlepass.command.quests",
    true  // player-only
) {
    override fun onExecute(sender: Player, args: MutableList<String>) {
        if (args.isEmpty()) {
            Messages.sendQuestsUsage(sender)
            return
        }

        val pass = (sender as CommandSender).resolveBattlePass(args.getOrNull(0)) ?: return

        val categoryId = args.getOrNull(1)

        if (categoryId == null) {
            CategoriesGUI(sender, pass).open()
            return
        }

        val category = pass.categories.firstOrNull {
            it.id.equals(categoryId, ignoreCase = true)
        } ?: run {
            Messages.sendInvalidCategory(sender)
            return
        }

        QuestsGUI(sender, category, wasBack = false).open()
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when (args.size) {
            1 -> {
                StringUtil.copyPartialMatches(
                    args[0],
                    BattlePasses.values().map { it.id },
                    mutableListOf()
                )
            }
            2 -> {
                val pass = BattlePasses.getByID(args.getOrNull(0) ?: "")
                val categoryIds = pass?.categories?.map { it.id } ?: emptyList()
                StringUtil.copyPartialMatches(args[1], categoryIds, mutableListOf())
            }
            else -> emptyList()
        }
    }
}