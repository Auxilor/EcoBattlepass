package com.exanthiax.ecobattlepass.libreforge.triggers

import com.exanthiax.ecobattlepass.api.events.PlayerTaskCompleteEvent
import org.bukkit.event.EventHandler
import wiki.ecohub.game.entity.toDispatcher
import wiki.ecohub.rules.trigger.Trigger
import wiki.ecohub.rules.trigger.TriggerData
import wiki.ecohub.rules.trigger.TriggerParameter

object TriggerBPTaskComplete: Trigger("complete_battlepass_task") {
    override val description = "Fires when the player completes a battlepass task."

    override val categories = setOf("player")

    override val parameters: Set<TriggerParameter> = setOf(
        TriggerParameter.PLAYER,
        TriggerParameter.EVENT,
        TriggerParameter.TEXT
    )

    override val parameterDescriptions = mapOf(
        TriggerParameter.TEXT to "The ID of the completed task"
    )

    @EventHandler(ignoreCancelled = true)
    fun handleLevelUp(event: PlayerTaskCompleteEvent) {
        this.dispatch(
            event.player.toDispatcher(),
            TriggerData(
                dispatcher = event.player.toDispatcher(),
                player = event.player,
                text = event.task.parent.id,
                event = event
            )
        )
    }
}