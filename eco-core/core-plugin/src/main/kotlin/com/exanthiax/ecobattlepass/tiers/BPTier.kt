package com.exanthiax.ecobattlepass.tiers

import com.exanthiax.ecobattlepass.api.hasPremium
import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.exanthiax.ecobattlepass.plugin
import com.exanthiax.ecobattlepass.rewards.Rewards
import com.exanthiax.ecobattlepass.utils.InternalPlaceholders
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.util.formatEco
import org.bukkit.entity.Player

class BPTier(val config: Config, val battlepass: BattlePass) {
    constructor(num: Int, battlepass: BattlePass) : this(
        Config.builder().add("tier", num),
        battlepass
    )

    val number = config.getInt("tier")
    val rewards = config.getSubsections("rewards").map { BPReward(it) }
    val saveId = "bptier_$number"
    val saveIdFree = "bptier_${number}_free"
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

    fun isFormatted(line: String): Boolean {
        return line.startsWith("&") || line.startsWith("§")
    }

    fun format(strings: List<String>, player: Player): List<String> {
        val result = mutableListOf<String>()

        for (string in strings) {
            when {
                string.contains("%free-rewards%") -> handleRewards(
                    result, string, player, TierType.FREE,
                    "%free-rewards%", "tiers-gui.buttons.free-rewards-format"
                )

                string.contains("%premium-rewards%") -> handleRewards(
                    result, string, player, TierType.PREMIUM,
                    "%premium-rewards%", null
                ) { _, p ->
                    if (p.hasPremium(battlepass)) {
                        "tiers-gui.buttons.premium-rewards-format"
                    } else {
                        "tiers-gui.buttons.missing-premium-rewards-format"
                    }
                }

                string.contains("%claimed-free-rewards%") -> handleRewards(
                    result, string, player, TierType.FREE,
                    "%claimed-free-rewards%", "tiers-gui.buttons.claimed-free-rewards-format"
                )

                string.contains("%claimed-premium-rewards%") -> handleRewards(
                    result, string, player, TierType.PREMIUM,
                    "%claimed-premium-rewards%", "tiers-gui.buttons.claimed-premium-rewards-format"
                )

                else -> result.add(InternalPlaceholders.TierPlaceholders.replace(string, this, battlepass, player))
            }
        }

        return result.formatEco(player, true)
    }

    fun format(singleString: String, player: Player): List<String> {
        return format(listOf(singleString), player)
    }

    private fun handleRewards(
        result: MutableList<String>,
        string: String,
        player: Player,
        tierType: TierType,
        placeholder: String,
        defaultFormatPath: String?,
        formatSelector: ((rewardLine: String, player: Player) -> String)? = null
    ) {
        val rwds = getRewardsFormatted(tierType, player)

        if (rwds.isNotEmpty()) {
            for (rewardLine in rwds) {
                if (isFormatted(rewardLine)) {
                    result.add(string.replace(placeholder, rewardLine))
                } else {
                    val formatPath = formatSelector?.invoke(rewardLine, player) ?: defaultFormatPath
                    val format = plugin.configYml.getFormattedString(formatPath!!)
                    result.add(string.replace(placeholder, format.replace("%reward%", rewardLine)))
                }
            }
        } else {
            result.add(
                string.replace(
                    placeholder,
                    plugin.configYml.getFormattedString("tiers-gui.buttons.empty-rewards-format")
                )
            )
        }
    }
}

class BPReward(val config: Config) : Tiered {
    val reward
        get() = Rewards.getByID(config.getString("id"))
            ?: throw IllegalArgumentException("Could not find reward with id ${config.getString("id")}")
    override val tier = TierType.entries.first {
        it.name.equals(config.getString("tier"), true)
    }
}
