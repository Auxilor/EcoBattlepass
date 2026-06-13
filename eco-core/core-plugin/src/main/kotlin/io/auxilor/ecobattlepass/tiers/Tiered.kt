package io.auxilor.ecobattlepass.tiers

import io.auxilor.ecobattlepass.api.hasPremium
import io.auxilor.ecobattlepass.battlepass.BattlePass
import io.auxilor.ecobattlepass.plugin
import org.bukkit.entity.Player

interface Tiered {
    val tier: TierType

    fun isAllowed(player: Player, pass: BattlePass): Boolean {
        return when (tier) {
            TierType.FREE -> true
            TierType.PREMIUM -> player.hasPremium(pass)
        }
    }

    val formattedName: String
        get() = plugin.langYml.getFormattedString(this.tier.name.lowercase())
}