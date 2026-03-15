package com.exanthiax.ecobattlepass.commands.dynamic

import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.commands.helpers.Messages
import com.exanthiax.ecobattlepass.gui.BattlePassGUI
import com.exanthiax.ecobattlepass.gui.BattleTiersGUI
import com.exanthiax.ecobattlepass.gui.QuestsGUI
import com.exanthiax.ecobattlepass.plugin
import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class DynamicPassCommand(
    private val pass: BattlePass,
    cmd: String
) : PluginCommand(
    plugin,
    cmd,
    "ecobattlepass.command.$cmd",
    true  // player-only
) {
    override fun onExecute(sender: Player, args: MutableList<String>) {
        when (args.getOrNull(0)?.lowercase()) {
            null -> {
                BattlePassGUI.createAndOpen(sender, pass)
            }

            "tiers" -> {
                BattleTiersGUI.createAndOpen(sender, pass)
            }

            "quests" -> {
                val categoryId = args.getOrNull(1) ?: run {
                    Messages.sendCategoryRequired(sender)
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

            else -> {
                Messages.sendDynamicPassUsage(sender)
            }
        }
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when (args.size) {
            1 -> StringUtil.copyPartialMatches(
                args[0],
                listOf("tiers", "quests"),
                mutableListOf()
            )
            2 -> {
                if (args[0].equals("quests", ignoreCase = true)) {
                    StringUtil.copyPartialMatches(
                        args[1],
                        pass.categories.map { it.id },
                        mutableListOf()
                    )
                } else emptyList()
            }
            else -> emptyList()
        }
    }
}