package com.exanthiax.ecobattlepass.commands.helpers

import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.plugin
import com.willfp.eco.util.toNiceString
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Centralized access to lang.yml messages.
 *
 * - "send" functions: directly send a message to a CommandSender (for errors/usage)
 * - "get" functions: return the raw string for further placeholder replacement (for success/feedback messages)
 */
object Messages {

    // ===== Error / Requirement Messages =====
    fun sendPlayerRequired(sender: CommandSender) =
        sender.sendMessage(plugin.langYml.getMessage("player-required"))

    fun sendPlayerNotFound(sender: CommandSender) =
        sender.sendMessage(plugin.langYml.getMessage("player-not-found"))

    fun sendPassRequired(sender: CommandSender) =
        sender.sendMessage(plugin.langYml.getMessage("pass-required"))

    fun sendPassNotFound(sender: CommandSender) =
        sender.sendMessage(plugin.langYml.getMessage("pass-not-found"))

    fun sendAmountRequired(sender: CommandSender) =
        sender.sendMessage(plugin.langYml.getMessage("amount-required"))

    fun sendInvalidAmount(sender: CommandSender) =
        sender.sendMessage(plugin.langYml.getMessage("invalid-amount"))

    fun sendCategoryRequired(sender: CommandSender) =
        sender.sendMessage(plugin.langYml.getMessage("category-required"))

    fun sendQuestRequired(sender: CommandSender) =
        sender.sendMessage(plugin.langYml.getMessage("quest-required"))

    fun sendTaskRequired(sender: CommandSender) =
        sender.sendMessage(plugin.langYml.getMessage("task-required"))

    fun sendInvalidCategory(sender: CommandSender) =
        sender.sendMessage(plugin.langYml.getMessage("invalid-category"))

    fun sendInvalidQuest(sender: CommandSender) =
        sender.sendMessage(plugin.langYml.getMessage("invalid-quest"))

    fun sendInvalidTask(sender: CommandSender) =
        sender.sendMessage(plugin.langYml.getMessage("invalid-task"))

    // ===== Give Command Success Messages =====
    fun getGivenExperience(): String = plugin.langYml.getMessage("given-experience")
    fun getReceivedExperience(): String = plugin.langYml.getMessage("received-experience")

    fun getGivenTiers(): String = plugin.langYml.getMessage("given-tiers")
    fun getReceivedTiers(): String = plugin.langYml.getMessage("received-tiers")

    fun getGivenTaskProgress(): String = plugin.langYml.getMessage("given-task-progress")
    fun getReceivedTaskProgress(): String = plugin.langYml.getMessage("received-task-progress")

    // ===== Complete Task =====
    fun getCompletedTask(): String = plugin.langYml.getMessage("completed-task")
    fun getTaskAlreadyCompleted(): String = plugin.langYml.getMessage("task-already-completed")

    // ===== Reset Command =====
    fun getResetPlayer(): String = plugin.langYml.getMessage("reset-player")
    fun getResetTask(): String = plugin.langYml.getMessage("reset-task")

    // ===== Set Premium Command =====
    fun getAlreadyPremium(): String = plugin.langYml.getMessage("already-premium")
    fun getNotPremium(): String = plugin.langYml.getMessage("not-premium")

    fun getPremiumGiven(): String = plugin.langYml.getMessage("premium-given")
    fun getPremiumRemoved(): String = plugin.langYml.getMessage("premium-removed")

    fun getPremiumUnlocked(): String = plugin.langYml.getMessage("premium-unlocked")
    fun getPremiumRevoked(): String = plugin.langYml.getMessage("premium-revoked")

    fun getPremiumBroadcast(): String = plugin.langYml.getMessage("premium-broadcast")

    // ===== Usage / Help =====
    fun sendGiveUsage(sender: CommandSender) =
        sender.sendMessage("§cUsage: /ecobattlepass give <xp|tiers|taskxp> <player|all> <pass> [args...]")

    fun sendSetPremiumUsage(sender: CommandSender) =
        sender.sendMessage("§cUsage: /ecobattlepass setpremium <player> <pass> [true|false] [silent]")

    fun sendQuestsUsage(sender: CommandSender) =
        sender.sendMessage("§cUsage: /ecobattlepass quests [pass] [category]")

    fun sendCompleteTaskUsage(sender: CommandSender) =
        sender.sendMessage("§cUsage: /ecobattlepass compelete_task <player|all> <pass> <category> <quest> <task>")

    fun sendTiersUsage(sender: CommandSender) =
        sender.sendMessage("§cUsage: /ecobattlepass tiers <pass>")

    fun sendResetUsage(sender: CommandSender) =
        sender.sendMessage("§cUsage: /ecobattlepass reset <pass|task> <player|all> <pass> [category] [quest] [task]")

    fun sendDynamicPassUsage(sender: CommandSender) =
        sender.sendMessage("§cUsage: /<pass> [tiers|quests <category>]")
}

/**
 * Extension function to replace common placeholders in feedback messages.
 * Used for success/info messages that include player name, amount, pass, and optionally task.
 */
fun String.replacePlaceholders(
    player: Player,
    amount: Number,
    pass: BattlePass,
    taskName: String? = null
): String {
    var message = this
        .replace("%playername%", player.name)
        .replace("%pass%", pass.name)

    message = when (amount) {
        is Double -> message.replace("%amount%", amount.toNiceString())
        else -> message.replace("%amount%", amount.toString())
    }

    if (taskName != null) {
        message = message.replace("%task%", taskName)
    }

    return message
}