package com.exanthiax.ecobattlepass.libreforge.triggers

import com.exanthiax.ecobattlepass.api.events.PlayerPostRewardEvent
import com.willfp.libreforge.toDispatcher
import com.willfp.libreforge.triggers.Trigger
import com.willfp.libreforge.triggers.TriggerData
import com.willfp.libreforge.triggers.TriggerParameter
import org.bukkit.event.EventHandler

object TriggerBPRewardClaim: Trigger("claim_battlepass_reward") {
    override val description = "Fires when the player claims a battlepass reward."

    override val categories = setOf("player")

    override val parameters: Set<TriggerParameter> = setOf(
        TriggerParameter.PLAYER,
        TriggerParameter.EVENT,
        TriggerParameter.TEXT
    )

    override val parameterDescriptions = mapOf(
        TriggerParameter.TEXT to "The ID of the battlepass reward that was claimed"
    )

    @EventHandler(ignoreCancelled = true)
    fun handleLevelUp(event: PlayerPostRewardEvent) {
        this.dispatch(
            event.player.toDispatcher(),
            TriggerData(
                dispatcher = event.player.toDispatcher(),
                player = event.player,
                text = event.reward.id,
                event = event
            )
        )
    }
}