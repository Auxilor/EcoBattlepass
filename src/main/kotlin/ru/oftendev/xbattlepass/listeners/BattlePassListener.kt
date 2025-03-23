package ru.oftendev.xbattlepass.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import ru.oftendev.xbattlepass.ConfiguredSound
import ru.oftendev.xbattlepass.api.bpTier
import ru.oftendev.xbattlepass.api.events.PlayerBPExpGainEvent
import ru.oftendev.xbattlepass.api.events.PlayerQuestCompleteEvent
import ru.oftendev.xbattlepass.api.events.PlayerRewardEvent
import ru.oftendev.xbattlepass.api.events.PlayerTierLevelUpEvent
import ru.oftendev.xbattlepass.api.giveBPExperience
import ru.oftendev.xbattlepass.api.receiveTier
import ru.oftendev.xbattlepass.battlepass.BattlePass
import ru.oftendev.xbattlepass.plugin

object BattlePassListener: Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun handleBPLevelUp(event: PlayerTierLevelUpEvent) {
        if (event.player.bpTier >= BattlePass.maxLevel) {
            event.isCancelled = true
            return
        }

        event.player.sendMessage(
            plugin.langYml.getMessage("tier-up").replace(
                "%tier%", event.level.toString()
            )
        )

        ConfiguredSound(
            plugin.configYml.getSubsection("sound.tier-up")
        ).play(event.player)

        // val tier = BattlePass.getTier(event.level) ?: return

        // event.player.receiveTier(tier)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun handleBPExp(event: PlayerBPExpGainEvent) {
        if (event.player.bpTier >= BattlePass.maxLevel) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun handleQuest(event: PlayerQuestCompleteEvent) {
        event.player.sendMessage(
            plugin.langYml.getMessage("quest-complete").replace(
                "%quest%", event.quest.getFormattedName(event.player)
            )
        )

        ConfiguredSound(
            plugin.configYml.getSubsection("sound.quest-complete")
        ).play(event.player)

        event.player.giveBPExperience(
            event.quest.parent.tierPoints.toDouble(),
            true
        )
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun handleReward(event: PlayerRewardEvent) {
        event.player.sendMessage(
            plugin.langYml.getMessage("reward-claim").replace(
                "%reward%", event.reward.id
            )
        )

        ConfiguredSound(
            plugin.configYml.getSubsection("sound.reward-claim")
        ).play(event.player)
    }
}