package com.exanthiax.ecobattlepass.libreforge.effects

import com.exanthiax.ecobattlepass.api.events.PlayerBPExpGainEvent
import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import org.bukkit.event.EventHandler
import wiki.ecohub.config.ArgType
import wiki.ecohub.config.arguments
import wiki.ecohub.game.entity.toDispatcher
import wiki.ecohub.rules.effect.MultiMultiplierEffect

object EffectBPExpMultiplier : MultiMultiplierEffect<BattlePass>("battlepass_xp_multiplier") {
    override val description = "Multiplies battlepass XP earned for one or all battlepasses while the holder is active."
    override val categories = setOf("economy")

    override val arguments = arguments {
        require(
            "multiplier",
            "You must specify the multiplier!",
            description = "The multiplier to apply. Supports expressions.",
            type = ArgType.EXPRESSION
        )
        optional(
            "battlepasses",
            description = "List of battlepass IDs to apply the multiplier to. If omitted, applies to all battlepasses.",
            type = ArgType.STRING_LIST
        )
    }

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