package com.exanthiax.ecobattlepass.commands.dynamic

import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.commands.helpers.ClaimHandler
import com.exanthiax.ecobattlepass.commands.helpers.Messages
import com.exanthiax.ecobattlepass.commands.reject
import com.exanthiax.ecobattlepass.commands.replying
import com.exanthiax.ecobattlepass.gui.BattlePassGUI
import com.exanthiax.ecobattlepass.gui.BattleTiersGUI
import com.exanthiax.ecobattlepass.gui.QuestsGUI
import com.exanthiax.ecobattlepass.plugin
import org.bukkit.entity.Player
import wiki.ecohub.core.command.CommandContext
import wiki.ecohub.core.command.EcoCommand
import wiki.ecohub.core.command.playerCommand

private val actions = listOf("tiers", "quests", "claim")
private val claimTypes = listOf("free", "premium")

private const val CLAIM_TYPE_ARGS = 3

/**
 * The per-pass command: the pass GUI, `tiers`, `quests <category>` and `claim <tier|all> [type]`.
 */
class DynamicPassCommand(
    private val pass: BattlePass,
    cmd: String
) {
    private val command: EcoCommand = plugin.playerCommand(name = cmd, permission = "ecobattlepass.command.$cmd") {
        executes {
            replying { run() }
        }

        completes {
            completions()
        }
    }

    fun register() {
        command.register()
    }

    private fun CommandContext<Player>.run() {
        when (args.getOrNull(0)?.lowercase()) {
            null -> BattlePassGUI.createAndOpen(sender, pass)
            "tiers" -> BattleTiersGUI.createAndOpen(sender, pass)
            "quests" -> openQuests()
            "claim" -> ClaimHandler.handleClaim(sender, pass, args)
            else -> reject(Messages::sendDynamicPassUsage)
        }
    }

    private fun CommandContext<Player>.openQuests() {
        val categoryId = argument(1, "category-required")
        val category = pass.categories.firstOrNull { it.id.equals(categoryId, ignoreCase = true) }
            ?: fail("invalid-category")

        QuestsGUI(sender, category, wasBack = false).open()
    }

    private fun CommandContext<Player>.completions(): List<String> = when {
        args.size == 1 -> actions
        args.size == 2 && args[0].equals("quests", ignoreCase = true) -> pass.categories.map { it.id }
        args.size == 2 && args[0].equals("claim", ignoreCase = true) -> pass.tiers.map { it.number.toString() } + "all"
        args.size == CLAIM_TYPE_ARGS && args[0].equals("claim", ignoreCase = true) -> claimTypes
        else -> emptyList()
    }
}
