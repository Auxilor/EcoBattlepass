package ru.oftendev.xbattlepass.libreforge.effects

import com.willfp.libreforge.effects.templates.MultiMultiplierEffect
import com.willfp.libreforge.effects.templates.MultiplierEffect
import com.willfp.libreforge.toDispatcher
import org.bukkit.entity.Bat
import org.bukkit.event.EventHandler
import ru.oftendev.xbattlepass.api.events.PlayerBPExpGainEvent
import ru.oftendev.xbattlepass.api.events.PlayerTaskExpGainEvent
import ru.oftendev.xbattlepass.tasks.BattleTask
import ru.oftendev.xbattlepass.tasks.BattleTasks

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