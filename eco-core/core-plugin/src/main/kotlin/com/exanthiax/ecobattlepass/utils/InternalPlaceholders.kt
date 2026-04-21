package com.exanthiax.ecobattlepass.utils

import com.exanthiax.ecobattlepass.api.getPassExp
import com.exanthiax.ecobattlepass.api.getTier
import com.exanthiax.ecobattlepass.api.hasReceivedTier
import com.exanthiax.ecobattlepass.api.taskProgress
import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.categories.Category
import com.exanthiax.ecobattlepass.plugin
import com.exanthiax.ecobattlepass.tasks.ActiveBattleTask
import com.exanthiax.ecobattlepass.tiers.BPTier
import com.willfp.eco.core.placeholder.PlayerDynamicPlaceholder
import com.willfp.eco.core.placeholder.PlayerPlaceholder
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.formatWithCommas
import com.willfp.eco.util.toNiceString
import com.willfp.eco.util.toNumeral
import org.bukkit.entity.Player
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import java.time.LocalDateTime

object InternalPlaceholders {

    private val regex by lazy { Regex("%tier_(-?\\d+)(_numeral)?%") }

    object BattlePassPlaceholders {
        fun register(battlepass: BattlePass) {
            PlayerlessPlaceholder(plugin, "${battlepass.id}_name") {
                battlepass.name
            }.register()

            PlayerPlaceholder(plugin, "completed_quests_${battlepass.id}") { player ->
                battlepass.categories.sumOf { it.getCompleted(player) }.toString()
            }.register()

            PlayerlessPlaceholder(plugin, "quest_amount_${battlepass.id}") {
                battlepass.categories.sumOf { it.quests.size }.toString()
            }.register()

            PlayerlessPlaceholder(plugin, "week_${battlepass.id}") {
                getWeekPlaceholder(battlepass)
            }.register()

            PlayerlessPlaceholder(plugin, "time_to_next_week_${battlepass.id}") {
                getTimeToNextWeekPlaceholder(battlepass)
            }.register()

            PlayerlessPlaceholder(plugin, "time_to_season_end_${battlepass.id}") {
                getTimeToSeasonEndPlaceholder(battlepass)
            }.register()

            PlayerPlaceholder(plugin, "has_unclaimed_rewards_${battlepass.id}") { player ->
                getBoolean(battlepass, player)
            }.register()

            PlayerlessPlaceholder(plugin, "${battlepass.id}_max_tiers") {
                battlepass.maxLevel.toString()
            }.register()

            PlayerlessPlaceholder(plugin, "${battlepass.id}_max_tiers_numeral") {
                battlepass.maxLevel.toNumeral()
            }.register()

            PlayerDynamicPlaceholder(plugin, Pattern.compile("tier_state_${battlepass.id}_\\d+$")) { string, player ->
                val tierToken = string.split("_").last()
                val requestedTier = tierToken.toIntOrNull()
                    ?: return@PlayerDynamicPlaceholder "Invalid tier $tierToken"
                player.hasReceivedTier(battlepass, requestedTier).toString()
            }.register()

            PlayerPlaceholder(plugin, "${battlepass.id}_pass_type") { player ->
                getPassType(battlepass, player)
            }.register()

            PlayerPlaceholder(plugin, "tier_${battlepass.id}") { player ->
                player.getTier(battlepass).toNiceString()
            }.register()

            PlayerPlaceholder(plugin, "tier_${battlepass.id}_numeral") { player ->
                player.getTier(battlepass).toNumeral()
            }.register()

            PlayerPlaceholder(plugin, "xp_required_${battlepass.id}") { player ->
                battlepass.getFormattedRequired(player)
            }.register()

            PlayerPlaceholder(plugin, "xp_required_${battlepass.id}_formatted") { player ->
                getFormattedRequiredXpForDisplay(battlepass, player)
            }.register()

            PlayerPlaceholder(plugin, "xp_${battlepass.id}") { player ->
                player.getPassExp(battlepass).toNiceString()
            }.register()

            PlayerPlaceholder(plugin, "xp_${battlepass.id}_formatted") { player ->
                player.getPassExp(battlepass).formatWithCommas()
            }.register()

            PlayerPlaceholder(plugin, "claimable_${battlepass.id}") { player ->
                battlepass.getClaimable(player).toNiceString()
            }.register()

            PlayerPlaceholder(plugin, "${battlepass.id}_percentage_progress") { player ->
                battlepass.getFormattedProgress(player)
            }.register()
        }

        fun replace(input: String, battlepass: BattlePass, player: Player): String {
            val tier = player.getTier(battlepass)
            var result = applyBattlePassReplacements(
                input = input,
                battlepass = battlepass,
                player = player,
                tier = tier,
                nextTier = tier + 1
            )
                .formatEco(player = player, formatPlaceholders = true)

            result = regex.replace(result) { match ->
                val offset = match.groupValues[1].toIntOrNull() ?: return@replace match.value
                val isNumeral = match.groupValues[2].isNotEmpty()
                val newTier = tier + offset
                if (isNumeral) newTier.toNumeral() else newTier.toNiceString()
            }

            return result
        }

        fun replaceAll(inputs: List<String>, battlepass: BattlePass, player: Player) =
            inputs.map { replace(it, battlepass, player) }
    }

    object TierPlaceholders {
        fun replace(input: String, tier: BPTier, battlepass: BattlePass, player: Player): String {
            var result = applyBattlePassReplacements(
                input = input,
                battlepass = battlepass,
                player = player,
                tier = tier.number,
                nextTier = tier.number + 1
            )
                .formatEco(player = player, formatPlaceholders = true)

            result = regex.replace(result) { match ->
                val offset = match.groupValues[1].toIntOrNull() ?: return@replace match.value
                val isNumeral = match.groupValues[2].isNotEmpty()
                val newTier = tier.number + offset
                if (isNumeral) newTier.toNumeral() else newTier.toNiceString()
            }

            return result
        }

        fun replaceAll(inputs: List<String>, tier: BPTier, battlepass: BattlePass, player: Player) =
            inputs.map { replace(it, tier, battlepass, player) }
    }

    object CategoryPlaceholders {
        fun register(category: Category) {
            PlayerPlaceholder(plugin, "category_completed_quests_${category.id}") { player ->
                category.getCompleted(player).toString()
            }.register()

            PlayerlessPlaceholder(plugin, "category_quest_amount_${category.id}") {
                category.quests.size.toString()
            }.register()

            PlayerlessPlaceholder(plugin, "category_${category.id}_start_date") {
                category.startDate.format(getDateFormatter())
            }.register()

            PlayerlessPlaceholder(plugin, "category_${category.id}_start_timer") {
                val millisLeft = category.startDate.atZone(ZoneId.systemDefault()).toInstant()
                    .toEpochMilli() - System.currentTimeMillis()
                if (millisLeft <= 0) {
                    plugin.langYml.getFormattedString("category-in-progress")
                } else {
                    msToString(millisLeft)
                }
            }.register()

            PlayerlessPlaceholder(plugin, "category_${category.id}_end_date") {
                category.endDate?.format(getDateFormatter())
            }.register()

            PlayerlessPlaceholder(plugin, "category_${category.id}_end_timer") {
                val duration = category.config.getInt("duration")
                if (duration == -1) {
                    plugin.langYml.getFormattedString("infinity")
                } else {
                    val millisLeft = category.endDate!!.atZone(ZoneId.systemDefault()).toInstant()
                        .toEpochMilli() - System.currentTimeMillis()
                    if (millisLeft <= 0) {
                        plugin.langYml.getFormattedString("category-expired")
                    } else {
                        msToString(millisLeft)
                    }
                }
            }.register()

            PlayerlessPlaceholder(plugin, "category_${category.id}_reset_timer") {
                val resetTime = category.config.getInt("reset-time")
                if (resetTime <= 0) {
                    plugin.langYml.getFormattedString("infinity")
                } else {
                    val nextReset = category.getNextResetDate()
                    if (nextReset != null) {
                        val millisLeft = nextReset.atZone(ZoneId.systemDefault()).toInstant()
                            .toEpochMilli() - System.currentTimeMillis()
                        msToString(millisLeft.coerceAtLeast(0))
                    } else {
                        msToString(0)
                    }
                }
            }.register()
        }

        fun replace(input: String, category: Category, player: Player): String {
            return input
                .replace("%category_name%", category.name).toNiceString()
                .replace("%category_id%", category.id)
                .replace("%pass%", category.battlepass.name).toNiceString()
                .replace("%pass_id%", category.battlepass.id)
                .replace("%completed%", category.getCompleted(player).toString())
                .replace("%total%", category.quests.size.toString())
                .replace("%time%", msToString(category.getDisplayableMs()))
                .formatEco(player = player, formatPlaceholders = true)
        }

        fun replaceAll(inputs: List<String>, category: Category, player: Player): List<String> =
            inputs.map { replace(it, category, player) }
    }

    object TaskPlaceholders {
        fun replace(input: String, task: ActiveBattleTask, player: Player): String {
            return input
                .replace("%task%", task.parent.name)
                .replace("%task_id%", task.parent.id)
                .replace("%current_task_xp%", player.taskProgress(task).toNiceString())
                .replace("%current_task_xp_formatted%", player.taskProgress(task).formatWithCommas())
                .replace("%required_task_xp%", task.requiredXP.toNiceString())
                .replace("%required_task_xp_formatted%", task.requiredXP.formatWithCommas())
                .formatEco(player = player, formatPlaceholders = true)
        }

        fun replaceAll(inputs: List<String>, task: ActiveBattleTask, player: Player): List<String> =
            inputs.map { replace(it, task, player) }
    }

    private fun getPassType(battlepass: BattlePass, player: Player): String {
        return plugin.langYml.getString(
            if (player.hasPermission(battlepass.premiumPerm)) "pass-type.premium" else "pass-type.free"
        )
    }

    private fun getBoolean(battlepass: BattlePass, player: Player): String? {
        return plugin.langYml.getString(
            if (battlepass.getClaimable(player) > 0) "yes" else "no"
        )
    }

    private fun applyBattlePassReplacements(
        input: String,
        battlepass: BattlePass,
        player: Player,
        tier: Int,
        nextTier: Int
    ): String {
        val currentExp = player.getPassExp(battlepass)
        return input
            .replace("%pass%", battlepass.name).toNiceString()
            .replace("%pass_id%", battlepass.id)
            .replace("%claimable_tiers%", battlepass.getClaimable(player).toNiceString())
            .replace("%max_tiers%", battlepass.maxLevel.toNiceString())
            .replace("%pass_type%", getPassType(battlepass, player))
            .replace("%start_date%", battlepass.startDate.format(getDateFormatter()))
            .replace("%end_date%", battlepass.endDate.format(getDateFormatter()))
            .replace("%percentage_progress%", battlepass.getFormattedProgress(player))
            .replace("%current_bp_xp%", currentExp.toNiceString())
            .replace("%current_bp_xp_formatted%", currentExp.formatWithCommas())
            .replace("%required_bp_xp%", battlepass.getFormattedRequired(player))
            .replace("%required_bp_xp_formatted%", getFormattedRequiredXpForDisplay(battlepass, player))
            .replace("%tier%", tier.toNiceString())
            .replace("%tier_numeral%", tier.toNumeral())
            .replace("%next_tier%", nextTier.toNiceString())
            .replace("%next_tier_numeral%", nextTier.toNumeral())
    }

    private fun getDateFormatter(): DateTimeFormatter {
        return DateTimeFormatter.ofPattern(plugin.configYml.getString("date-format"))
    }

    private fun getFormattedRequiredXpForDisplay(battlepass: BattlePass, player: Player): String {
        val requiredXp = battlepass.getExpForLevel(player.getTier(battlepass) + 1)
        return if (requiredXp.isInfinite()) {
            plugin.langYml.getFormattedString("infinity")
        } else {
            requiredXp.formatWithCommas()
        }
    }

    private fun getWeekPlaceholder(battlepass: BattlePass): String {
        val now = LocalDateTime.now()
        if (now.isBefore(battlepass.startDate)) {
            return plugin.langYml.getFormattedString("season-not-started")
        }
        if (now.isAfter(battlepass.endDate)) {
            return plugin.langYml.getFormattedString("season-finished")
        }

        val weekCategories = battlepass.categories.filter { it.config.getInt("priority") > 0 }
        val activeWeek = weekCategories.filter { it.isActive }
            .maxByOrNull { it.config.getInt("priority") }

        if (activeWeek != null) {
            return activeWeek.config.getInt("priority").toString()
        }

        val allWeeksEnded = weekCategories.all { cat ->
            cat.endDate != null && now.isAfter(cat.endDate)
        }

        return if (allWeeksEnded) {
            plugin.langYml.getFormattedString("season-finished")
        } else {
            val lastEndedWeek = weekCategories
                .filter { cat -> cat.endDate != null && now.isAfter(cat.endDate) }
                .maxByOrNull { it.config.getInt("priority") }

            lastEndedWeek?.config?.getInt("priority")?.toString()
                ?: plugin.langYml.getFormattedString("waiting-for-week")
        }
    }

    private fun getTimeToNextWeekPlaceholder(battlepass: BattlePass): String {
        val now = LocalDateTime.now()
        if (now.isBefore(battlepass.startDate)) {
            return plugin.langYml.getFormattedString("season-not-started")
        }
        if (now.isAfter(battlepass.endDate)) {
            return plugin.langYml.getFormattedString("season-finished")
        }

        val weekCategories = battlepass.categories.filter { it.config.getInt("priority") > 0 }
        val nextWeek = weekCategories
            .filter { it.startDate.isAfter(now) }
            .minByOrNull { it.startDate }

        return if (nextWeek == null) {
            plugin.langYml.getFormattedString("season-finished")
        } else {
            val millisLeft = nextWeek.startDate
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli() - System.currentTimeMillis()
            msToString(millisLeft.coerceAtLeast(0))
        }
    }

    private fun getTimeToSeasonEndPlaceholder(battlepass: BattlePass): String {
        val now = LocalDateTime.now()
        if (now.isBefore(battlepass.startDate)) {
            return plugin.langYml.getFormattedString("season-not-started")
        }
        if (now.isAfter(battlepass.endDate)) {
            return plugin.langYml.getFormattedString("season-finished")
        }

        val millisLeft = battlepass.endDate.atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli() - System.currentTimeMillis()
        return msToString(millisLeft.coerceAtLeast(0))
    }

}
