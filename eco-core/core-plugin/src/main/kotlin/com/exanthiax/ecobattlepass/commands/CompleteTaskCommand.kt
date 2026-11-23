package com.exanthiax.ecobattlepass.commands

import com.exanthiax.ecobattlepass.api.checkCompletedQuest
import com.exanthiax.ecobattlepass.api.hasCompletedTask
import com.exanthiax.ecobattlepass.api.setCompletedTask
import com.exanthiax.ecobattlepass.commands.helpers.Messages
import com.exanthiax.ecobattlepass.commands.helpers.replacePlaceholders
import com.exanthiax.ecobattlepass.runOwned
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder

internal fun CommandBuilder<CommandSender>.completeTaskCommand() = subcommand(
    name = "complete_task",
    permission = "ecobattlepass.command.completetask"
) {
    executes {
        replying {
            if (args.isEmpty()) {
                reject(Messages::sendCompleteTaskUsage)
            }

            val players = players(0)
            val pass = battlePass(1)
            val task = task(pass, taskIds())
            val toComplete = players.filterNot { it.hasCompletedTask(task) }

            for (player in toComplete) {
                player.runOwned {
                    player.setCompletedTask(task, true)
                    player.checkCompletedQuest(task)
                }
            }

            val message = if (toComplete.isEmpty()) Messages.getTaskAlreadyCompleted() else Messages.getCompletedTask()

            sender.sendMessage(
                message.replacePlaceholders(
                    player = players.first(),
                    amount = 1,
                    pass = pass,
                    task = task,
                    taskName = task.parent.name
                ).replace(oldValue = "%playername%", newValue = displayName(players))
            )
        }
    }

    completes {
        taskCompletions()
    }
}
