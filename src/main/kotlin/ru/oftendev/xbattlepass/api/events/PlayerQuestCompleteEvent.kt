package ru.oftendev.xbattlepass.api.events

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import ru.oftendev.xbattlepass.quests.ActiveBattleQuest

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