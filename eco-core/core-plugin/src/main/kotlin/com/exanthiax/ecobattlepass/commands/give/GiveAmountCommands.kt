package com.exanthiax.ecobattlepass.commands.give

import com.exanthiax.ecobattlepass.api.giveExactBPExperience
import com.exanthiax.ecobattlepass.api.giveExactBPTiers
import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.commands.battlePass
import com.exanthiax.ecobattlepass.commands.commonAmounts
import com.exanthiax.ecobattlepass.commands.displayName
import com.exanthiax.ecobattlepass.commands.helpers.Messages
import com.exanthiax.ecobattlepass.commands.helpers.replacePlaceholders
import com.exanthiax.ecobattlepass.commands.passIds
import com.exanthiax.ecobattlepass.commands.playerNamesAndAll
import com.exanthiax.ecobattlepass.commands.players
import com.exanthiax.ecobattlepass.runOwned
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.core.command.CommandContext

private const val AMOUNT_ARGS = 3

/**
 * `give xp <player|all> <pass> <amount>`. A single player's sender message waits for their
 * region; for `all` it is sent once, straight away.
 */
internal fun CommandBuilder<CommandSender>.giveXPCommand() = giveAmountCommand(
    name = "xp",
    parse = { it.toDoubleOrNull() },
    given = Messages::getGivenExperience,
    received = Messages::getReceivedExperience
) { player, pass, amount -> player.giveExactBPExperience(pass, amount.toDouble()) }

/**
 * `give tiers <player|all> <pass> <amount>`, sent as [giveXPCommand] sends.
 */
internal fun CommandBuilder<CommandSender>.giveTiersCommand() = giveAmountCommand(
    name = "tiers",
    parse = { it.toIntOrNull() },
    given = Messages::getGivenTiers,
    received = Messages::getReceivedTiers
) { player, pass, amount -> player.giveExactBPTiers(pass, amount.toInt()) }

private fun CommandBuilder<CommandSender>.giveAmountCommand(
    name: String,
    parse: (String) -> Number?,
    given: () -> String,
    received: () -> String,
    give: (Player, BattlePass, Number) -> Unit
) = subcommand(name = name, permission = "ecobattlepass.command.give.$name") {
    executes {
        val players = players(0)
        val pass = battlePass(1)
        val amount = argument(index = 2, missingKey = "amount-required", invalidKey = "invalid-amount", parse = parse)
        val isAll = players.size > 1

        for (player in players) {
            player.runOwned {
                give(player, pass, amount)
                player.sendMessage(received().replacePlaceholders(player, amount, pass))

                if (!isAll) {
                    sendGiven(given(), players, pass, amount)
                }
            }
        }

        if (isAll) {
            sendGiven(given(), players, pass, amount)
        }
    }

    completes {
        when (args.size) {
            1 -> playerNamesAndAll()
            2 -> passIds()
            AMOUNT_ARGS -> commonAmounts
            else -> emptyList()
        }
    }
}

private fun CommandContext<CommandSender>.sendGiven(
    message: String,
    players: List<Player>,
    pass: BattlePass,
    amount: Number
) {
    sender.sendMessage(
        message.replacePlaceholders(player = players.first(), amount = amount, pass = pass)
            .replace(oldValue = "%playername%", newValue = displayName(players))
    )
}
