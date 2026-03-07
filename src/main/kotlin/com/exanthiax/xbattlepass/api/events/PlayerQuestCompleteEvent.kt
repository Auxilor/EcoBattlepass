package com.exanthiax.xbattlepass.api.events

import com.exanthiax.xbattlepass.quests.ActiveBattleQuest
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class PlayerQuestCompleteEvent(player: Player,
                               val quest: ActiveBattleQuest): PlayerEvent(player) {
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