package com.exanthiax.ecobattlepass.commands.reset

import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.exanthiax.ecobattlepass.categories.Categories
import com.exanthiax.ecobattlepass.commands.helpers.Messages
import com.exanthiax.ecobattlepass.commands.helpers.TaskTabCompleter
import com.exanthiax.ecobattlepass.commands.helpers.replacePlaceholders
import com.exanthiax.ecobattlepass.commands.helpers.resolveBattlePass
import com.exanthiax.ecobattlepass.commands.helpers.resolvePlayers
import com.exanthiax.ecobattlepass.plugin
import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

object ResetTaskSubcommand : Subcommand(
    plugin,
    "task",
    "ecobattlepass.command.reset.task",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        val players = sender.resolvePlayers(args.getOrNull(0)) ?: return
        val pass = sender.resolveBattlePass(args.getOrNull(1)) ?: return

        val categoryId = args.getOrNull(2) ?: run {
            Messages.sendCategoryRequired(sender)
            return
        }
        val questId = args.getOrNull(3) ?: run {
            Messages.sendQuestRequired(sender)
            return
        }
        val taskId = args.getOrNull(4) ?: run {
            Messages.sendTaskRequired(sender)
            return
        }

        val category = Categories.getByID(categoryId) ?: run {
            Messages.sendInvalidCategory(sender)
            return
        }

        if (category.battlepass != pass) {
            Messages.sendInvalidCategory(sender)
            return
        }

        val activeQuest = category.quests.find { it.parent.id.equals(questId, true) } ?: run {
            Messages.sendInvalidQuest(sender)
            return
        }

        val activeTask = activeQuest.tasks.find { it.parent.id.equals(taskId, true) } ?: run {
            Messages.sendInvalidTask(sender)
            return
        }

        val isAll = players.size > 1
        val displayName = if (isAll) "all players" else players.first().name

        for (player in players) {
            activeTask.reset(player)
        }

        val baseMessage = Messages.getResetTask()

        sender.sendMessage(
            baseMessage.replacePlaceholders(
                player = players.first(),
                amount = 0,
                pass = pass,
                taskName = activeTask.parent.name
            ).replace("%playername%", displayName)
        )
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when (args.size) {
            1 -> StringUtil.copyPartialMatches(
                args[0],
                Bukkit.getOnlinePlayers().map { it.name } + "all",
                mutableListOf()
            )
            2 -> StringUtil.copyPartialMatches(
                args[1],
                BattlePasses.values().map { it.id },
                mutableListOf()
            )
            3 -> TaskTabCompleter.forCategory(args[2])
            4 -> TaskTabCompleter.forQuest(args.getOrNull(2) ?: "", args[3])
            5 -> TaskTabCompleter.forTask(args.getOrNull(2) ?: "", args.getOrNull(3) ?: "", args[4])
            else -> emptyList()
        }
    }
}