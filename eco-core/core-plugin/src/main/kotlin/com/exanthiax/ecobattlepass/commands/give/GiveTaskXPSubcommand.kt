package com.exanthiax.ecobattlepass.commands.give

import com.exanthiax.ecobattlepass.api.giveTaskExperience
import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.exanthiax.ecobattlepass.categories.Categories
import com.exanthiax.ecobattlepass.commands.helpers.COMMON_AMOUNTS
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

object GiveTaskXPSubcommand : Subcommand(
    plugin,
    "taskxp",
    "ecobattlepass.command.give.taskxp",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
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
        val amountString = args.getOrNull(5) ?: run {
            Messages.sendAmountRequired(sender)
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

        val amount = amountString.toDoubleOrNull() ?: run {
            Messages.sendInvalidAmount(sender)
            return
        }

        for (player in players) {
            player.giveTaskExperience(activeTask, amount)

            val baseGiven = Messages.getGivenTaskProgress()
            val baseReceived = Messages.getReceivedTaskProgress()

            sender.sendMessage(
                baseGiven.replacePlaceholders(player, amount, pass, taskName = activeTask.parent.name)
            )

            player.sendMessage(
                baseReceived.replacePlaceholders(player, amount, pass, taskName = activeTask.parent.name)
            )
        }
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when (args.size) {
            1 -> StringUtil.copyPartialMatches(args[0], Bukkit.getOnlinePlayers().map { it.name } + "all", mutableListOf())
            2 -> StringUtil.copyPartialMatches(args[1], BattlePasses.values().map { it.id }, mutableListOf())
            3 -> TaskTabCompleter.forCategory(args[2])
            4 -> TaskTabCompleter.forQuest(args.getOrNull(2) ?: "", args[3])
            5 -> TaskTabCompleter.forTask(args.getOrNull(2) ?: "", args.getOrNull(3) ?: "", args[4])
            6 -> StringUtil.copyPartialMatches(args[5], COMMON_AMOUNTS, mutableListOf())
            else -> emptyList()
        }
    }
}