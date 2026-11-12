package com.exanthiax.ecobattlepass.rewards

import com.exanthiax.ecobattlepass.api.events.PlayerPostRewardEvent
import com.exanthiax.ecobattlepass.api.events.PlayerPreRewardEvent
import com.exanthiax.ecobattlepass.plugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import wiki.ecohub.config.Config
import wiki.ecohub.config.ViolationContext
import wiki.ecohub.core.registry.Registrable
import wiki.ecohub.game.entity.toDispatcher
import wiki.ecohub.rules.effect.Effects
import wiki.ecohub.rules.trigger.TriggerData
import wiki.ecohub.ui.text.formatEco

class Reward(private val _id: String, val config: Config) : Registrable {
    val id: String
        get() = _id

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Reward) return false
        return _id == other._id
    }

    override fun hashCode(): Int = _id.hashCode()

    override fun toString(): String = "Reward(id='$_id')"
}