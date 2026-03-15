package com.exanthiax.ecobattlepass.api.events

import com.exanthiax.ecobattlepass.battlepass.BattlePass
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class PlayerBPExpGainEvent(player: Player,
                           val battlepass: BattlePass,
                           private var _amount: Double,
                           val isMultiply: Boolean): PlayerEvent(player), Cancellable {
    private var cancelled = false

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(p0: Boolean) {
        cancelled = p0
    }

    fun setAmount(amount: Double) {
        this._amount = amount
    }

    fun getAmount(): Double {
        return this._amount
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }
}