package com.exanthiax.ecobattlepass.commands.reset

import com.exanthiax.ecobattlepass.commands.battlePass
import com.exanthiax.ecobattlepass.commands.displayName
import com.exanthiax.ecobattlepass.commands.helpers.Messages
import com.exanthiax.ecobattlepass.commands.helpers.replacePlaceholders
import com.exanthiax.ecobattlepass.commands.passIds
import com.exanthiax.ecobattlepass.commands.playerNamesAndAll
import com.exanthiax.ecobattlepass.commands.players
import com.exanthiax.ecobattlepass.commands.task
import com.exanthiax.ecobattlepass.commands.taskCompletions
import com.exanthiax.ecobattlepass.commands.taskIds
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder

internal fun CommandBuilder<CommandSender>.resetBattlepassCommand() = subcommand(
    name = "battlepass",
    permission = "ecobattlepass.command.reset.battlepass"
) {
    executes {
        val players = players(0)
        val pass = battlePass(1)

        for (player in players) {
            pass.reset(player)
        }

        sender.sendMessage(
            Messages.getResetPlayer()
                .replacePlaceholders(player = players.first(), amount = 0, pass = pass)
                .replace(oldValue = "%playername%", newValue = displayName(players))
        )
    }

    completes {
        when (args.size) {
            1 -> playerNamesAndAll()
            2 -> passIds()
            else -> emptyList()
        }
    }
}

internal fun CommandBuilder<CommandSender>.resetTaskCommand() = subcommand(
    name = "task",
    permission = "ecobattlepass.command.reset.task"
) {
    executes {
        val players = players(0)
        val pass = battlePass(1)
        val task = task(pass, taskIds())

        for (player in players) {
            task.reset(player)
        }

        sender.sendMessage(
            Messages.getResetTask()
                .replacePlaceholders(
                    player = players.first(),
                    amount = 0,
                    pass = pass,
                    task = task,
                    taskName = task.parent.name
                )
                .replace(oldValue = "%playername%", newValue = displayName(players))
        )
    }

    completes {
        taskCompletions()
    }
}
