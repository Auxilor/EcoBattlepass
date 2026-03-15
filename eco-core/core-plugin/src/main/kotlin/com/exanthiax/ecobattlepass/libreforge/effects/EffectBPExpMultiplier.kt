package com.exanthiax.ecobattlepass.libreforge.effects

import com.exanthiax.ecobattlepass.api.events.PlayerBPExpGainEvent
import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.willfp.libreforge.effects.templates.MultiMultiplierEffect
import com.willfp.libreforge.toDispatcher
import org.bukkit.event.EventHandler

object EffectBPExpMultiplier : MultiMultiplierEffect<BattlePass>("battlepass_xp_multiplier") {
    override val key = "battlepasses"

    override fun getElement(key: String): BattlePass? {
        return BattlePasses.getByID(key)
    }

    override fun getAllElements(): Collection<BattlePass> {
        return BattlePasses.values()
    }

    @EventHandler(ignoreCancelled = true)
    fun handle(event: PlayerBPExpGainEvent) {
        val player = event.player

        event.setAmount(event.getAmount() * getMultiplier(player.toDispatcher(), event.battlepass))
    }
}