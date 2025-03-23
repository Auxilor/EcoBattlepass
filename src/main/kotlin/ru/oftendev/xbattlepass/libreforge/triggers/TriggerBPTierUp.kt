package ru.oftendev.xbattlepass.libreforge.triggers

import com.willfp.libreforge.toDispatcher
import com.willfp.libreforge.triggers.Trigger
import com.willfp.libreforge.triggers.TriggerData
import com.willfp.libreforge.triggers.TriggerParameter
import org.bukkit.event.EventHandler
import ru.oftendev.xbattlepass.api.events.PlayerTierLevelUpEvent

object TriggerBPTierUp: Trigger("tier_up_battlepass") {
    override val parameters: Set<TriggerParameter> = setOf(
        TriggerParameter.PLAYER,
        TriggerParameter.EVENT
    )

    @EventHandler(ignoreCancelled = true)
    fun handleLevelUp(event: PlayerTierLevelUpEvent) {
        this.dispatch(
            event.player.toDispatcher(),
            TriggerData(
                dispatcher = event.player.toDispatcher(),
                player = event.player,
                event = event,
                value = event.level.toDouble()
            )
        )
    }
}