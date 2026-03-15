package com.exanthiax.ecobattlepass.commands

import com.exanthiax.ecobattlepass.api.checkCompletedQuest
import com.exanthiax.ecobattlepass.api.hasCompletedTask
import com.exanthiax.ecobattlepass.api.setCompletedTask
import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.exanthiax.ecobattlepass.categories.Categories
import com.exanthiax.ecobattlepass.commands.helpers.Messages
import com.exanthiax.ecobattlepass.commands.helpers.TaskTabCompleter
import com.exanthiax.ecobattlepass.commands.helpers.replacePlaceholders
import com.exanthiax.ecobattlepass.commands.helpers.resolveBattlePass
import com.exanthiax.ecobattlepass.commands.helpers.resolvePlayers
import com.exanthiax.ecobattlepass.plugin
import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

object CompleteTaskCommand : PluginCommand(
    plugin,
    "complete_task",
    "ecobattlepass.command.completetask",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (args.isEmpty()) {
            Messages.sendCompleteTaskUsage(sender)
            return
        }

        val players = sender.resolvePlayers(args.getOrNull(0)) ?: return
        val pass = sender.resolveBattlePass(args.getOrNull(1)) ?: return

        val categoryString = args.getOrNull(2) ?: run {
            Messages.sendCategoryRequired(sender)
            return
        }
        val questString = args.getOrNull(3) ?: run {
            Messages.sendQuestRequired(sender)
            return
        }
        val taskString = args.getOrNull(4) ?: run {
            Messages.sendTaskRequired(sender)
            return
        }

        val category = Categories.getByID(categoryString) ?: run {
            Messages.sendInvalidCategory(sender)
            return
        }

        if (category.battlepass != pass) {
            Messages.sendInvalidCategory(sender)
            return
        }

        val activeQuest = category.quests.find { it.parent.id.equals(questString, true) } ?: run {
            Messages.sendInvalidQuest(sender)
            return
        }

        val activeTask = activeQuest.tasks.find { it.parent.id.equals(taskString, true) } ?: run {
            Messages.sendInvalidTask(sender)
            return
        }

        var anyCompleted = false

        for (player in players) {
            if (player.hasCompletedTask(activeTask)) {
                continue  // Skip if already completed
            }

            player.setCompletedTask(activeTask, true)
            player.checkCompletedQuest(activeTask)
            anyCompleted = true
        }

        val isAll = players.size > 1
        val displayName = if (isAll) "all players" else players.first().name

        val baseMessage = if (anyCompleted) {
            Messages.getCompletedTask()
        } else {
            Messages.getTaskAlreadyCompleted()
        }

        sender.sendMessage(
            baseMessage.replacePlaceholders(
                player = players.first(),
                amount = 1,
                pass = pass,
                taskName = activeTask.parent.name
            ).replace("%playername%", displayName)
        )
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when (args.size) {
            1 -> StringUtil.copyPartialMatches(args[0], Bukkit.getOnlinePlayers().map { it.name } + "all", mutableListOf())
            2 -> StringUtil.copyPartialMatches(args[1], BattlePasses.values().map { it.id }, mutableListOf())
            3 -> TaskTabCompleter.forCategory(args[2])
            4 -> TaskTabCompleter.forQuest(args.getOrNull(2) ?: "", args[3])
            5 -> TaskTabCompleter.forTask(args.getOrNull(2) ?: "", args.getOrNull(3) ?: "", args[4])
            else -> emptyList()
        }
    }
}