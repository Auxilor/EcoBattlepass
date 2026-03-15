package com.exanthiax.ecobattlepass.utils

import com.exanthiax.ecobattlepass.api.events.PlayerBPExpGainEvent
import com.exanthiax.ecobattlepass.api.events.PlayerPostRewardEvent
import com.exanthiax.ecobattlepass.api.events.PlayerQuestCompleteEvent
import com.exanthiax.ecobattlepass.api.events.PlayerTierLevelUpEvent
import com.exanthiax.ecobattlepass.api.getTier
import com.exanthiax.ecobattlepass.api.giveBPExperience
import com.exanthiax.ecobattlepass.plugin
import com.willfp.eco.core.sound.PlayableSound
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

object BattlePassListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun handleBPLevelUp(event: PlayerTierLevelUpEvent) {
        val player = event.player

        if (event.player.getTier(event.battlepass) >= event.battlepass.maxLevel) {
            event.isCancelled = true
            return
        }

        event.player.sendMessage(
            plugin.langYml.getMessage("tier-up").replace(
                "%tier%", event.level.toString()
            )
        )
        PlayableSound.create(plugin.configYml.getSubsection("sound.tier-up"))?.playTo(player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun handleBPExp(event: PlayerBPExpGainEvent) {
        if (event.player.getTier(event.battlepass) >= event.battlepass.maxLevel) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun handleQuest(event: PlayerQuestCompleteEvent) {
        val player = event.player

        event.player.sendMessage(
            plugin.langYml.getMessage("quest-complete").replace(
                "%quest%", event.quest.getFormattedName(event.player)
            )
        )
        PlayableSound.create(plugin.configYml.getSubsection("sound.quest-complete"))?.playTo(player)

        event.player.giveBPExperience(
            event.quest.category.battlepass,
            event.quest.parent.tierPoints.toDouble(),
            true
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun handleReward(event: PlayerPostRewardEvent) {
        val player = event.player

        event.player.sendMessage(
            plugin.langYml.getMessage("reward-claim").replace(
                "%reward%", event.reward.getDisplayName(event.player)
            )
        )
        PlayableSound.create(plugin.configYml.getSubsection("sound.reward-claim"))?.playTo(player)
    }
}