package com.exanthiax.ecobattlepass.libreforge.effects

import com.exanthiax.ecobattlepass.api.events.PlayerTaskExpGainEvent
import com.exanthiax.ecobattlepass.tasks.BattleTask
import com.exanthiax.ecobattlepass.tasks.BattleTasks
import com.willfp.libreforge.effects.templates.MultiMultiplierEffect
import com.willfp.libreforge.toDispatcher
import org.bukkit.event.EventHandler

object EffectTaskExpMultiplier : MultiMultiplierEffect<BattleTask>("battlepass_task_xp_multiplier") {
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