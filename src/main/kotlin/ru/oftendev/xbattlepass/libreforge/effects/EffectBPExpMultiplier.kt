package ru.oftendev.xbattlepass.libreforge.effects

import com.willfp.libreforge.effects.templates.MultiMultiplierEffect
import com.willfp.libreforge.effects.templates.MultiplierEffect
import com.willfp.libreforge.toDispatcher
import org.bukkit.event.EventHandler
import ru.oftendev.xbattlepass.api.events.PlayerBPExpGainEvent

object EffectBPExpMultiplier : MultiplierEffect("battlepass_xp_multiplier") {
    @EventHandler(ignoreCancelled = true)
    fun handle(event: PlayerBPExpGainEvent) {
        val player = event.player

        if (event.isMultiply) {
            event.setAmount(event.getAmount() * getMultiplier(player.toDispatcher()))
        }
    }
}