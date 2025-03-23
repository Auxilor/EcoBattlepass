package ru.oftendev.xbattlepass.battlepass

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.toNiceString
import com.willfp.eco.util.toNumeral
import org.bukkit.entity.Bat
import org.bukkit.entity.Player
import ru.oftendev.xbattlepass.api.bpPassExp
import ru.oftendev.xbattlepass.api.bpTier
import ru.oftendev.xbattlepass.rewards.Rewards

class BPTier(val config: Config) {
    constructor(num: Int) : this(
        Config.builder().add("tier", num)
    )

    val number = config.getInt("tier")
    val rewards = config.getSubsections("rewards").map { BPReward(it) }
    val saveId = "bptier_$number"
    val transient = false

    fun getRewardsFormatted(tierType: TierType, player: Player): List<String> {
        val result = mutableListOf<String>()
        val format = BattlePass.getRewardsFormat(tierType)
        for (reward in rewards) {
            if (reward.tier != tierType) continue
            result.addAll(
                reward.reward.rewardLoreUnformatted.map {
                    format.replace("%reward%", it)
                }
            )
        }
        return result.formatEco(player = player)
    }

    fun format(string: String, player: Player): String {
        return string.replace("%percentage_progress%", BattlePass.getFormattedProgress(player))
            .replace("%current_xp%", player.bpPassExp.toNiceString())
            .replace("%required_xp%", BattlePass.getFormattedRequired(player))
            .replace("%tier%", this.number.toNiceString())
            .replace("%tier_numeral%", this.number.toNumeral())
    }

    fun format(strings: List<String>, player: Player): List<String> {
        val result = mutableListOf<String>()

        for (string in strings) {
            if (string.contains("%free-rewards%")) {
                val rwds = getRewardsFormatted(TierType.FREE, player)

                result.addAll(
                    rwds.map { string.replace("%free-rewards%", it) }
                )
            } else if (string.contains("%premium-rewards%")) {
                val rwds = getRewardsFormatted(TierType.PREMIUM, player)
                result.addAll(
                    rwds.map { string.replace("%premium-rewards%", it) }
                )
            } else {
                result.add(
                    string.replace("%percentage_progress%", BattlePass.getFormattedProgress(player))
                        .replace("%current_xp%", player.bpPassExp.toNiceString())
                        .replace("%required_xp%", BattlePass.getFormattedRequired(player))
                        .replace("%tier%", this.number.toNiceString())
                        .replace("%tier_numeral%", this.number.toNumeral())
                )
            }
        }

        return result.formatEco(player)
    }
}

class BPReward(val config: Config): Tiered {
    val reward = Rewards.getByID(config.getString("id"))!!
    override val tier = TierType.entries.first {
        it.name.equals(config.getString("tier"), true)
    }
}