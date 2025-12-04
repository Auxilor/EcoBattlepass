package com.exanthiax.xbattlepass.libreforge.effects

import com.willfp.libreforge.effects.templates.MultiMultiplierEffect
import com.willfp.libreforge.effects.templates.MultiplierEffect
import com.willfp.libreforge.toDispatcher
import org.bukkit.event.EventHandler
import com.exanthiax.xbattlepass.api.events.PlayerBPExpGainEvent
import com.exanthiax.xbattlepass.battlepass.BattlePass
import com.exanthiax.xbattlepass.battlepass.BattlePasses

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