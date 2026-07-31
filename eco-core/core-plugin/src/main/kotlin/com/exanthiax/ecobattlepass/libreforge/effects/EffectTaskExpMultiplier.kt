package com.exanthiax.ecobattlepass.libreforge.effects

import com.exanthiax.ecobattlepass.api.events.PlayerTaskExpGainEvent
import com.exanthiax.ecobattlepass.tasks.BattleTask
import com.exanthiax.ecobattlepass.tasks.BattleTasks
import com.willfp.libreforge.ArgType
import com.willfp.libreforge.arguments
import com.willfp.libreforge.effects.templates.MultiMultiplierEffect
import com.willfp.libreforge.toDispatcher
import org.bukkit.event.EventHandler

object EffectTaskExpMultiplier : MultiMultiplierEffect<BattleTask>("battlepass_task_xp_multiplier") {
    override val description = "Multiplies task XP earned for one or all battlepass tasks while the holder is active."
    override val categories = setOf("economy")

    override val arguments = arguments {
        require(
            "multiplier",
            "You must specify the multiplier!",
            description = "The multiplier to apply. Supports expressions.",
            type = ArgType.EXPRESSION
        )
        optional(
            "tasks",
            description = "List of task IDs to apply the multiplier to. If omitted, applies to all tasks.",
            type = ArgType.STRING_LIST
        )
    }

    override val key = "tasks"

    override fun getElement(key: String): BattleTask? {
        return BattleTasks.getByID(key)
    }

    override fun getAllElements(): Collection<BattleTask> {
        return BattleTasks.values()
    }

    @EventHandler(ignoreCancelled = true)
    fun handle(event: PlayerTaskExpGainEvent) {
        val player = event.player

        event.setAmount(event.getAmount() * getMultiplier(player.toDispatcher(), event.task.parent))
    }
}