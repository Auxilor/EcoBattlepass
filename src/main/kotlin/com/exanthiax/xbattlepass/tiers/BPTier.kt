package com.exanthiax.xbattlepass.tiers

import com.exanthiax.xbattlepass.api.getPassExp
import com.exanthiax.xbattlepass.battlepass.BattlePass
import com.exanthiax.xbattlepass.battlepass.BattlePasses
import com.exanthiax.xbattlepass.plugin
import com.exanthiax.xbattlepass.rewards.Rewards
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.formatWithCommas
import com.willfp.eco.util.toNiceString
import com.willfp.eco.util.toNumeral
import org.bukkit.entity.Player

class BPTier(val config: Config, val battlepass: BattlePass) {
    constructor(num: Int, battlepass: BattlePass) : this(
        Config.builder().add("tier", num),
        battlepass
    )

    val number = config.getInt("tier")
    val rewards = config.getSubsections("rewards").map { BPReward(it) }
    val saveId = "bptier_$number"
    val transient = false

    fun getRewardsFormatted(tierType: TierType, player: Player): List<String> {
        val result = mutableListOf<String>()
        val format = BattlePasses.getRewardsFormat(tierType)

        for (reward in rewards) {
            if (reward.tier != tierType) continue
            result.add(
                reward.reward.getDisplayName(player)
            )
            result.addAll(
                reward.reward.rewardLoreUnformatted.map {
                    format.replace("%reward%", it)
                }
            )
        }

        return result.formatEco(player, true)
    }

    private fun replaceBasicPlaceholders(input: String, player: Player): String {
        var result = input
            .replace("%pass%", battlepass.name)
            .replace("%claimable_tiers%", battlepass.getClaimable(player).toNiceString())
            .replace("%percentage_progress%", battlepass.getFormattedProgress(player))
            .replace("%current_bp_xp%", player.getPassExp(battlepass).toNiceString())
            .replace("%current_bp_xp_formatted%", player.getPassExp(battlepass).formatWithCommas())
            .replace("%required_bp_xp%", battlepass.getFormattedRequired(player))
            .replace("%required_bp_xp_formatted%", battlepass.getFormattedRequired(player).toDouble().formatWithCommas())
            .replace("%tier%", this.number.toNiceString())
            .replace("%tier_numeral%", this.number.toNumeral())
            .replace("%next_tier%", (this.number + 1).toNiceString())
            .replace("%next_tier_numeral%", (this.number + 1).toNumeral())

        val regex = Regex("%tier_(-?\\d+)(_numeral)?%")

        result = regex.replace(result) { match ->
            val offset = match.groupValues[1].toIntOrNull() ?: return@replace match.value
            val isNumeral = match.groupValues[2].isNotEmpty()
            val newTier = this.number + offset

            if (isNumeral) newTier.toNumeral() else newTier.toNiceString()
        }

        return result
    }


    fun isFormatted(line: String): Boolean {
        return line.startsWith("&") || line.startsWith("§")
    }

    fun format(strings: List<String>, player: Player): List<String> {
        val result = mutableListOf<String>()

        for (string in strings) {
            if (string.contains("%free-rewards%")) {
                val rwds = getRewardsFormatted(TierType.FREE, player)

                if (rwds.isNotEmpty()) {
                    for (rewardLine in rwds) {
                        if (isFormatted(rewardLine)) {
                            result.add(string.replace("%free-rewards%", rewardLine))
                        } else {
                            result.add(
                                string.replace(
                                    "%free-rewards%",
                                    plugin.configYml.getFormattedString("tiers-gui.buttons.free-rewards-format")
                                        .replace("%reward%", rewardLine)
                                )
                            )
                        }
                    }
                } else {
                    result.add(
                        string.replace(
                            "%free-rewards%", plugin.configYml
                                .getFormattedString("tiers-gui.buttons.empty-rewards-format")
                        )
                    )
                }
            } else if (string.contains("%premium-rewards%")) {
                val rwds = getRewardsFormatted(TierType.PREMIUM, player)
                if (rwds.isNotEmpty()) {
                    for (rewardLine in rwds) {
                        if (isFormatted(rewardLine)) {
                            result.add(string.replace("%premium-rewards%", rewardLine))
                        } else {
                            result.add(
                                string.replace(
                                    "%premium-rewards%",
                                    plugin.configYml.getFormattedString("tiers-gui.buttons.premium-rewards-format")
                                        .replace("%reward%", rewardLine)
                                )
                            )
                        }
                    }
                } else {
                    result.add(
                        string.replace(
                            "%premium-rewards%", plugin.configYml
                                .getFormattedString("tiers-gui.buttons.empty-rewards-format")
                        )
                    )
                }
            }
            else {
                result.add(replaceBasicPlaceholders(string, player))
            }
        }

        return result.formatEco(player, true)
    }

    fun format(singleString: String, player: Player): List<String> {
        return format(listOf(singleString), player)
    }
}

class BPReward(val config: Config): Tiered {
    val reward
        get() = Rewards.getByID(config.getString("id"))
            ?: throw IllegalArgumentException("Could not find reward with id ${config.getString("id")}")
    override val tier = TierType.entries.first {
        it.name.equals(config.getString("tier"), true)
    }
}
