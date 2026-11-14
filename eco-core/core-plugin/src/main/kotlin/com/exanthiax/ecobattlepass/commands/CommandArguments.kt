package com.exanthiax.ecobattlepass.commands

import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.exanthiax.ecobattlepass.categories.Categories
import com.exanthiax.ecobattlepass.tasks.ActiveBattleTask
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import wiki.ecohub.core.command.CommandContext

internal val commonAmounts = listOf("1", "10", "100", "1000")

private const val CATEGORY_INDEX = 2
private const val QUEST_INDEX = 3
private const val TASK_INDEX = 4

/**
 * Thrown by [reject] to stop a command with one of the raw usage messages.
 */
internal class CommandRejection(val send: (CommandSender) -> Unit) : RuntimeException(null, null, false, false)

/**
 * Stop the command, sending the sender a message with [send].
 */
internal fun reject(send: (CommandSender) -> Unit): Nothing = throw CommandRejection(send)

/**
 * Run [block], sending the message of any [reject] it calls.
 */
internal inline fun CommandContext<*>.replying(block: () -> Unit) {
    try {
        block()
    } catch (rejection: CommandRejection) {
        rejection.send(sender)
    }
}

/**
 * The players named by argument [index]: one online player, or every online player for `all`.
 */
internal fun CommandContext<*>.players(index: Int): List<Player> {
    val name = argument(index, "player-required")

    return if (name.equals("all", ignoreCase = true)) {
        Bukkit.getOnlinePlayers().toList()
    } else {
        listOf(Bukkit.getPlayer(name) ?: fail("player-not-found"))
    }
}

internal fun CommandContext<*>.battlePass(index: Int): BattlePass =
    BattlePasses.getByID(argument(index, "pass-required")) ?: fail("pass-not-found")

/**
 * The ids of `<category> <quest> <task>` from argument 2, each required.
 */
internal fun CommandContext<*>.taskIds(): Triple<String, String, String> = Triple(
    first = argument(CATEGORY_INDEX, "category-required"),
    second = argument(QUEST_INDEX, "quest-required"),
    third = argument(TASK_INDEX, "task-required")
)

/**
 * The active task named by [ids], in a category of [pass].
 */
internal fun CommandContext<*>.task(pass: BattlePass, ids: Triple<String, String, String>): ActiveBattleTask {
    val (categoryId, questId, taskId) = ids
    val category = Categories.getByID(categoryId)?.takeIf { it.battlepass == pass } ?: fail("invalid-category")
    val quest = category.quests.find { it.parent.id.equals(questId, true) } ?: fail("invalid-quest")
    return quest.tasks.find { it.parent.id.equals(taskId, true) } ?: fail("invalid-task")
}

internal fun playerNamesAndAll(): List<String> = Bukkit.getOnlinePlayers().map { it.name } + "all"

internal fun passIds(): List<String> = BattlePasses.values().map { it.id }

/**
 * Completions for `<player|all> <pass> <category> <quest> <task>`.
 */
internal fun CommandContext<*>.taskCompletions(): List<String> = when (args.size) {
    1 -> playerNamesAndAll()
    2 -> passIds()
    CATEGORY_INDEX + 1 -> Categories.values().map { it.id }
    QUEST_INDEX + 1 -> Categories.getByID(args[CATEGORY_INDEX])?.run { quests.map { it.parent.id } }.orEmpty()
    TASK_INDEX + 1 -> Categories.getByID(args[CATEGORY_INDEX])
        ?.run { quests.find { it.parent.id.equals(args[QUEST_INDEX], true) } }
        ?.run { tasks.map { it.parent.id } }
        .orEmpty()

    else -> emptyList()
}

/**
 * The name shown for [players]: theirs, or `all players`.
 */
internal fun displayName(players: List<Player>): String =
    if (players.size > 1) "all players" else players.first().name
