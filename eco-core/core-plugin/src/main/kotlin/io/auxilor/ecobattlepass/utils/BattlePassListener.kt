package io.auxilor.ecobattlepass.utils

import io.auxilor.ecobattlepass.api.events.PlayerBPExpGainEvent
import io.auxilor.ecobattlepass.api.events.PlayerPostRewardEvent
import io.auxilor.ecobattlepass.api.events.PlayerQuestCompleteEvent
import io.auxilor.ecobattlepass.api.events.PlayerTierLevelUpEvent
import io.auxilor.ecobattlepass.api.getTier
import io.auxilor.ecobattlepass.api.giveBPExperience
import io.auxilor.ecobattlepass.plugin
import com.willfp.eco.core.sound.PlayableSound
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

object BattlePassListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun handleBPLevelUp(event: PlayerTierLevelUpEvent) {
        val player = event.player

        if (event.player.getTier(event.battlepass) >= event.battlepass.maxLevel) {
            event.isCancelled = true
            return
        }

        event.player.sendMessage(
            InternalPlaceholders.BattlePassPlaceholders
                .replace(plugin.langYml.getMessage("tier-up"), event.battlepass, player)
                .replace("%tier%", event.level.toString())
        )
        PlayableSound.create(plugin.configYml.getSubsection("sound.tier-up"))?.playTo(player)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun handleBPExp(event: PlayerBPExpGainEvent) {
        if (event.player.getTier(event.battlepass) >= event.battlepass.maxLevel) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun handleQuest(event: PlayerQuestCompleteEvent) {
        val player = event.player

        val questMessage = plugin.langYml.getMessage("quest-complete")
            .replace("%quest%", event.quest.getFormattedName(event.player))

        event.player.sendMessage(
            InternalPlaceholders.BattlePassPlaceholders.replace(
                InternalPlaceholders.CategoryPlaceholders.replace(
                    questMessage,
                    event.quest.category,
                    event.player
                ),
                event.quest.category.battlepass,
                event.player
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