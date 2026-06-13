package io.auxilor.ecobattlepass.commands.dynamic

import io.auxilor.ecobattlepass.battlepass.BattlePass
import io.auxilor.ecobattlepass.commands.helpers.ClaimHandler
import io.auxilor.ecobattlepass.commands.helpers.Messages
import io.auxilor.ecobattlepass.gui.BattlePassGUI
import io.auxilor.ecobattlepass.gui.BattleTiersGUI
import io.auxilor.ecobattlepass.gui.QuestsGUI
import io.auxilor.ecobattlepass.plugin
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
    true
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

            "claim" -> {
                ClaimHandler.handleClaim(sender, pass, args)
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
                listOf("tiers", "quests", "claim"),
                mutableListOf()
            )

            2 -> when (args[0].lowercase()) {
                "quests" -> StringUtil.copyPartialMatches(
                    args[1],
                    pass.categories.map { it.id },
                    mutableListOf()
                )

                "claim" -> StringUtil.copyPartialMatches(
                    args[1],
                    pass.tiers.map { it.number.toString() } + "all",
                    mutableListOf()
                )

                else -> emptyList()
            }

            3 -> {
                if (args[0].equals("claim", ignoreCase = true)) {
                    StringUtil.copyPartialMatches(
                        args[2],
                        listOf("free", "premium"),
                        mutableListOf()
                    )
                } else emptyList()
            }

            else -> emptyList()
        }
    }
}
