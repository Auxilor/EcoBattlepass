package com.exanthiax.ecobattlepass.tiers

import com.exanthiax.ecobattlepass.api.hasPremium
import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.plugin
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