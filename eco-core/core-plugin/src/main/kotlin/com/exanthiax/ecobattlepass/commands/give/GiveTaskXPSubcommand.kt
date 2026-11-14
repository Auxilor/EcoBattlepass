package com.exanthiax.ecobattlepass.commands.give

import com.exanthiax.ecobattlepass.api.giveTaskExperience
import com.exanthiax.ecobattlepass.commands.battlePass
import com.exanthiax.ecobattlepass.commands.commonAmounts
import com.exanthiax.ecobattlepass.commands.helpers.Messages
import com.exanthiax.ecobattlepass.commands.helpers.replacePlaceholders
import com.exanthiax.ecobattlepass.commands.players
import com.exanthiax.ecobattlepass.commands.task
import com.exanthiax.ecobattlepass.commands.taskCompletions
import com.exanthiax.ecobattlepass.commands.taskIds
import com.exanthiax.ecobattlepass.runOwned
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder

private const val AMOUNT_INDEX = 5

internal fun CommandBuilder<CommandSender>.giveTaskXPCommand() = subcommand(
    name = "taskxp",
    permission = "ecobattlepass.command.give.taskxp"
) {
    executes {
        val players = players(0)
        val pass = battlePass(1)
        val ids = taskIds()
        val amountArg = argument(AMOUNT_INDEX, "amount-required")
        val task = task(pass, ids)
        val amount = amountArg.toDoubleOrNull() ?: fail("invalid-amount")

        for (player in players) {
            player.runOwned {
                player.giveTaskExperience(task, amount)

                val given = Messages.getGivenTaskProgress()
                    .replacePlaceholders(player, amount, pass, task, task.parent.name)
                val received = Messages.getReceivedTaskProgress()
                    .replacePlaceholders(player, amount, pass, task, task.parent.name)

                sender.sendMessage(given)
                player.sendMessage(received)
            }
        }
    }

    completes {
        if (args.size == AMOUNT_INDEX + 1) commonAmounts else taskCompletions()
    }
}
