package ru.oftendev.xbattlepass.battlepass

import org.bukkit.entity.Player
import ru.oftendev.xbattlepass.api.hasPremium
import ru.oftendev.xbattlepass.plugin

interface Tiered {
    val tier: TierType

    fun isAllowed(player: Player): Boolean {
        return when (tier) {
            TierType.FREE -> true
            TierType.PREMIUM -> player.hasPremium
        }
    }

    val formattedName: String
        get() = plugin.langYml.getFormattedString(this.tier.name.lowercase())
}