package com.exanthiax.ecobattlepass.libreforge.triggers

import com.exanthiax.ecobattlepass.api.events.PlayerBPExpGainEvent
import org.bukkit.event.EventHandler
import wiki.ecohub.game.entity.toDispatcher
import wiki.ecohub.rules.trigger.Trigger
import wiki.ecohub.rules.trigger.TriggerData
import wiki.ecohub.rules.trigger.TriggerParameter

object TriggerBPExpGain: Trigger("gain_battlepass_xp") {
    override val description = "Fires when the player gains battlepass experience."

    override val categories = setOf("player")

    override val parameters: Set<TriggerParameter> = setOf(
        TriggerParameter.PLAYER,
        TriggerParameter.EVENT,
        TriggerParameter.VALUE,
        TriggerParameter.TEXT
    )

    override val parameterDescriptions = mapOf(
        TriggerParameter.VALUE to "The amount of battlepass experience gained",
        TriggerParameter.TEXT to "The ID of the battlepass that the experience was gained for"
    )

    @EventHandler(ignoreCancelled = true)
    fun handleLevelUp(event: PlayerBPExpGainEvent) {
        this.dispatch(
            event.player.toDispatcher(),
            TriggerData(
                dispatcher = event.player.toDispatcher(),
                player = event.player,
                event = event,
                value = event.getAmount(),
                text = event.battlepass.id
            )
        )
    }
}