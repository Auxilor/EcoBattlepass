package com.exanthiax.ecobattlepass.libreforge.triggers

import com.exanthiax.ecobattlepass.api.events.PlayerTaskCompleteEvent
import com.willfp.libreforge.toDispatcher
import com.willfp.libreforge.triggers.Trigger
import com.willfp.libreforge.triggers.TriggerData
import com.willfp.libreforge.triggers.TriggerParameter
import org.bukkit.event.EventHandler

object TriggerBPTaskComplete: Trigger("complete_battlepass_task") {
    override val description = "Fires when the player completes a battlepass task."

    override val categories = setOf("player")

    override val parameters: Set<TriggerParameter> = setOf(
        TriggerParameter.PLAYER,
        TriggerParameter.EVENT,
        TriggerParameter.TEXT,
        TriggerParameter.VALUE
    )

    override val parameterDescriptions = mapOf(
        TriggerParameter.TEXT to "The ID of the completed task",
        TriggerParameter.VALUE to "Always 1.0, representing a single task completion"
    )

    @EventHandler(ignoreCancelled = true)
    fun handleLevelUp(event: PlayerTaskCompleteEvent) {
        this.dispatch(
            event.player.toDispatcher(),
            TriggerData(
                dispatcher = event.player.toDispatcher(),
                player = event.player,
                text = event.task.parent.id,
                event = event,
                value = 1.0
            )
        )
    }
}