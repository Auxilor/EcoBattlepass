package com.exanthiax.ecobattlepass.commands.helpers

import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.exanthiax.ecobattlepass.categories.Categories
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

/**
 * Resolves the target player(s) from the first argument.
 * Returns null and sends error message if invalid.
 */
fun CommandSender.resolvePlayers(arg: String?): List<Player>? {
    val playerString = arg ?: run {
        Messages.sendPlayerRequired(this)
        return null
    }

    return if (playerString.equals("all", ignoreCase = true)) {
        Bukkit.getOnlinePlayers().toList()
    } else {
        val player = Bukkit.getPlayer(playerString)
        if (player == null) {
            Messages.sendPlayerNotFound(this)
            return null
        }
        listOf(player)
    }
}

/**
 * Resolves the BattlePass from the argument.
 * Returns null and sends error message if not found.
 */
fun CommandSender.resolveBattlePass(arg: String?): BattlePass? {
    val passString = arg ?: run {
        Messages.sendPassRequired(this)
        return null
    }
    return BattlePasses.getByID(passString) ?: run {
        Messages.sendPassNotFound(this)
        return null
    }
}

/**
 * Shared amount suggestions for tab completion
 */
val COMMON_AMOUNTS = listOf("1", "10", "100", "1000")

/**
 * Tab completion helpers for task-related arguments (category, quest, task)
 * Used in multiple places across the plugin
 */
object TaskTabCompleter {
    fun forCategory(arg: String): List<String> {
        return StringUtil.copyPartialMatches(arg, Categories.values().map { it.id }, mutableListOf())
    }

    fun forQuest(categoryId: String, arg: String): List<String> {
        val category = Categories.getByID(categoryId) ?: return emptyList()
        return StringUtil.copyPartialMatches(arg, category.quests.map { it.parent.id }, mutableListOf())
    }

    fun forTask(categoryId: String, questId: String, arg: String): List<String> {
        val category = Categories.getByID(categoryId) ?: return emptyList()
        val quest = category.quests.find { it.parent.id.equals(questId, true) } ?: return emptyList()
        return StringUtil.copyPartialMatches(arg, quest.tasks.map { it.parent.id }, mutableListOf())
    }
}