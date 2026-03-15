package com.exanthiax.ecobattlepass.rewards

import com.exanthiax.ecobattlepass.api.events.PlayerPostRewardEvent
import com.exanthiax.ecobattlepass.api.events.PlayerPreRewardEvent
import com.exanthiax.ecobattlepass.plugin
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.registry.Registrable
import com.willfp.eco.util.formatEco
import com.willfp.libreforge.ViolationContext
import com.willfp.libreforge.effects.Effects
import com.willfp.libreforge.toDispatcher
import com.willfp.libreforge.triggers.TriggerData
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class Reward(private val _id: String, val config: Config) : Registrable {
    override fun getID(): String {
        return this._id
    }

    fun getDisplayName(player: Player): String {
        return this.config.getString("display.name").formatEco(player, true)
    }

    val rewardEffects = Effects.compileChain(
        config.getSubsections("effects"),
        ViolationContext(
            plugin,
            "EcoBattlepass reward $_id"
        )
    )

    val rewardLoreUnformatted = config.getStrings(
        "display.reward-lore"
    )

    fun grant(player: Player): Boolean {
        val preEvent = PlayerPreRewardEvent(player, this)
        Bukkit.getPluginManager().callEvent(preEvent)
        if (preEvent.isCancelled) return false

        rewardEffects?.trigger(
            player.toDispatcher(),
            TriggerData(
                player = player,
                dispatcher = player.toDispatcher(),
                value = 1.0,
                text = _id
            )
        )

        Bukkit.getPluginManager().callEvent(PlayerPostRewardEvent(player, this))

        return true
    }
}