package com.exanthiax.ecobattlepass.utils

import com.exanthiax.ecobattlepass.api.getPassExp
import com.exanthiax.ecobattlepass.api.getTier
import com.exanthiax.ecobattlepass.api.taskProgress
import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.categories.Category
import com.exanthiax.ecobattlepass.plugin
import com.exanthiax.ecobattlepass.tasks.ActiveBattleTask
import com.exanthiax.ecobattlepass.tiers.BPTier
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.formatWithCommas
import com.willfp.eco.util.toNiceString
import com.willfp.eco.util.toNumeral
import org.bukkit.entity.Player
import java.time.format.DateTimeFormatter

object InternalPlaceholders {

    object BattlePassPlaceholders {
        fun replace(input: String, battlepass: BattlePass, player: Player): String {
            var result =  input
                .replace("%pass%", battlepass.name).toNiceString()
                .replace("%pass_id%", battlepass.id)
                .replace("%claimable_tiers%", battlepass.getClaimable(player).toNiceString())
                .replace("%max_tiers%", battlepass.maxLevel.toNiceString())
                .replace("%pass_type%", plugin.langYml.getString(if (player.hasPermission(battlepass.premiumPerm)) "pass-type.premium" else "pass-type.free"))
                .replace("%start_date%", battlepass.startDate.format(DateTimeFormatter.ofPattern(plugin.configYml.getString("date-format"))))
                .replace("%end_date%", battlepass.endDate.format(DateTimeFormatter.ofPattern(plugin.configYml.getString("date-format"))))
                .replace("%percentage_progress%", battlepass.getFormattedProgress(player))
                .replace("%current_bp_xp%", player.getPassExp(battlepass).toNiceString())
                .replace("%current_bp_xp_formatted%", player.getPassExp(battlepass).formatWithCommas())
                .replace("%required_bp_xp%", battlepass.getFormattedRequired(player))
                .replace("%required_bp_xp_formatted%", battlepass.getFormattedRequired(player).toDouble().formatWithCommas())
                .replace("%tier%", player.getTier(battlepass).toNiceString())
                .replace("%tier_numeral%", player.getTier(battlepass).toNumeral())
                .replace("%next_tier%", (player.getTier(battlepass) + 1).toNiceString())
                .replace("%next_tier_numeral%", (player.getTier(battlepass) + 1).toNumeral())
                .formatEco(formatPlaceholders = true)

            val regex = Regex("%tier_(-?\\d+)(_numeral)?%")
            result = regex.replace(result) { match ->
                val offset = match.groupValues[1].toIntOrNull() ?: return@replace match.value
                val isNumeral = match.groupValues[2].isNotEmpty()
                val newTier = player.getTier(battlepass) + offset
                if (isNumeral) newTier.toNumeral() else newTier.toNiceString()
            }

            return result
        }

        fun replaceAll(inputs: List<String>, battlepass: BattlePass, player: Player) =
            inputs.map { replace(it, battlepass, player) }
    }

    object TierPlaceholders {
        fun replace(input: String, tier: BPTier, battlepass: BattlePass, player: Player): String {
            var result =  input
                .replace("%pass%", battlepass.name).toNiceString()
                .replace("%pass_id%", battlepass.id)
                .replace("%claimable_tiers%", battlepass.getClaimable(player).toNiceString())
                .replace("%max_tiers%", battlepass.maxLevel.toNiceString())
                .replace("%pass_type%", plugin.langYml.getString(if (player.hasPermission(battlepass.premiumPerm)) "pass-type.premium" else "pass-type.free"))
                .replace("%start_date%", battlepass.startDate.format(DateTimeFormatter.ofPattern(plugin.configYml.getString("date-format"))))
                .replace("%end_date%", battlepass.endDate.format(DateTimeFormatter.ofPattern(plugin.configYml.getString("date-format"))))
                .replace("%percentage_progress%", battlepass.getFormattedProgress(player))
                .replace("%current_bp_xp%", player.getPassExp(battlepass).toNiceString())
                .replace("%current_bp_xp_formatted%", player.getPassExp(battlepass).formatWithCommas())
                .replace("%required_bp_xp%", battlepass.getFormattedRequired(player))
                .replace("%required_bp_xp_formatted%", battlepass.getFormattedRequired(player).toDouble().formatWithCommas())
                .replace("%tier%", tier.number.toNiceString())
                .replace("%tier_numeral%", tier.number.toNumeral())
                .replace("%next_tier%", (tier.number + 1).toNiceString())
                .replace("%next_tier_numeral%", (tier.number + 1).toNumeral())
                .formatEco(formatPlaceholders = true)

            val regex = Regex("%tier_(-?\\d+)(_numeral)?%")
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
        fun replace(input: String, category: Category, player: Player): String {
            return input
                .replace("%category_name%", category.name).toNiceString()
                .replace("%category_id%", category.id)
                .replace("%pass%", category.battlepass.name).toNiceString()
                .replace("%pass_id%", category.battlepass.id)
                .replace("%completed%", category.getCompleted(player).toString())
                .replace("%total%", category.quests.size.toString())
                .replace("%time%", msToString(category.getDisplayableMs()))
                .formatEco(formatPlaceholders = true)
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
                .formatEco(formatPlaceholders = true)
        }

        fun replaceAll(inputs: List<String>, task: ActiveBattleTask, player: Player): List<String> =
            inputs.map { replace(it, task, player) }
    }
}
