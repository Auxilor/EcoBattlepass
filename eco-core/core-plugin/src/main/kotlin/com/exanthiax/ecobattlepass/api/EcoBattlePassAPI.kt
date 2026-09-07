package com.exanthiax.ecobattlepass.api

import com.exanthiax.ecobattlepass.api.events.PlayerBPExpGainEvent
import com.exanthiax.ecobattlepass.api.events.PlayerQuestCompleteEvent
import com.exanthiax.ecobattlepass.api.events.PlayerTaskCompleteEvent
import com.exanthiax.ecobattlepass.api.events.PlayerTierLevelUpEvent
import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.plugin
import com.exanthiax.ecobattlepass.quests.ActiveBattleQuest
import com.exanthiax.ecobattlepass.tasks.ActiveBattleTask
import com.exanthiax.ecobattlepass.tiers.BPTier
import com.exanthiax.ecobattlepass.tiers.TierType
import com.exanthiax.ecobattlepass.utils.ReceivedTierState
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.progression.LevelProgression
import com.willfp.eco.core.progression.StopReason
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionAttachment
import java.util.UUID

fun OfflinePlayer.getTier(pass: BattlePass): Int {
    return this.profile.read(pass.tierKey)
}

fun OfflinePlayer.setTier(pass: BattlePass, tier: Int) {
    this.profile.write(pass.tierKey, tier)
}

fun OfflinePlayer.getPassExp(pass: BattlePass): Double {
    return this.profile.read(pass.passExpKey)
}

fun OfflinePlayer.setPassExp(pass: BattlePass, passExp: Double) {
    this.profile.write(pass.passExpKey, passExp)
}

fun OfflinePlayer.getReceivedTiers(pass: BattlePass): List<String> {
    return this.profile.read(pass.receivedTiersKey)
}

fun OfflinePlayer.setReceivedTiers(pass: BattlePass, tiers: List<String>) {
    this.profile.write(pass.receivedTiersKey, tiers.distinct())
}

fun Player.hasPremium(pass: BattlePass): Boolean {
    return this.hasPermission(pass.premiumPerm)
}

private val playerAttachments = mutableMapOf<UUID, PermissionAttachment>()

fun Player.setPremium(pass: BattlePass, premium: Boolean) {
    val perm = pass.premiumPerm
    val attachment = playerAttachments.getOrPut(this.uniqueId) { this.addAttachment(plugin) }
    attachment.setPermission(perm, premium)
    this.recalculatePermissions()
}

fun Player.receiveTier(tier: BPTier) {
    val current = this.getReceivedTiers(tier.battlepass)
    val alreadyFree = tier.saveIdFree in current
    val alreadyPremium = tier.saveIdPremium in current

    // Only grant rewards that haven't been claimed separately
    tier.rewards.filter { reward ->
        reward.isAllowed(this, tier.battlepass) &&
                !(alreadyFree && reward.tier == TierType.FREE) &&
                !(alreadyPremium && reward.tier == TierType.PREMIUM)
    }.forEach {
        it.reward.grant(this)
    }

    // Consolidate save IDs
    val cleaned = current - tier.saveIdFree - tier.saveIdPremium
    this.setReceivedTiers(
        tier.battlepass,
        cleaned + if (this.hasPremium(tier.battlepass)) tier.saveId else tier.saveIdFree
    )
}

fun Player.receiveTierPremiumOnly(tier: BPTier) {
    tier.rewards.filter { it.tier == TierType.PREMIUM }.forEach {
        it.reward.grant(this)
    }

    val current = this.getReceivedTiers(tier.battlepass)

    if (tier.saveIdFree in current) {
        // Both sides claimed → consolidate to saveId
        this.setReceivedTiers(tier.battlepass, current - tier.saveIdFree + tier.saveId)
    } else {
        // Only premium claimed
        this.setReceivedTiers(tier.battlepass, current + tier.saveIdPremium)
    }
}

fun Player.receiveTierFreeOnly(tier: BPTier) {
    tier.rewards.filter { it.tier == TierType.FREE }.forEach {
        it.reward.grant(this)
    }

    val current = this.getReceivedTiers(tier.battlepass)

    if (tier.saveIdPremium in current) {
        // Both sides claimed → consolidate to saveId
        this.setReceivedTiers(tier.battlepass, current - tier.saveIdPremium + tier.saveId)
    } else {
        this.setReceivedTiers(tier.battlepass, current + tier.saveIdFree)
    }
}

fun OfflinePlayer.hasCompletedTask(task: ActiveBattleTask): Boolean {
    return this.profile.read(task.completedKey)
}

fun OfflinePlayer.hasCompletedQuest(quest: ActiveBattleQuest): Boolean {
    if (this.profile.read(quest.completedKey)) {
        return true
    } else {
        if (quest.tasks.all { this.hasCompletedTask(it) }) {
            this.profile.write(quest.completedKey, true)
            return true
        }
    }
    return false
}

fun OfflinePlayer.setCompletedQuest(quest: ActiveBattleQuest, value: Boolean) {
    this.profile.write(quest.completedKey, value)
}

fun OfflinePlayer.setCompletedTask(task: ActiveBattleTask, value: Boolean) {
    this.profile.write(task.completedKey, value)
}

fun OfflinePlayer.setTaskProgress(task: ActiveBattleTask, progress: Double) {
    this.profile.write(task.progressKey, progress)
}

fun OfflinePlayer.taskProgress(task: ActiveBattleTask): Double {
    return this.profile.read(task.progressKey)
}

fun Player.giveTaskExperience(task: ActiveBattleTask, amount: Double) {
    this.profile.write(task.progressKey, amount + this.taskProgress(task))

    if (this.taskProgress(task) >= task.requiredXP) {
        val event = PlayerTaskCompleteEvent(this, task)
        Bukkit.getPluginManager().callEvent(event)

        if (!event.isCancelled) {
            this.profile.write(task.completedKey, true)
            this.checkCompletedQuest(task)
        }
    }
}

fun Player.checkCompletedQuest(task: ActiveBattleTask) {
    if (this.hasCompletedQuest(task.quest)) {
        val event = PlayerQuestCompleteEvent(this, task.quest)
        Bukkit.getPluginManager().callEvent(event)
    }
}

fun Player.giveBPExperience(pass: BattlePass, experience: Double, withMultipliers: Boolean = true) {
    // Previously abs() turned a negative amount into a gain, hiding the misconfiguration.
    // giveExactBPExperience now refuses non-positive amounts instead.
    val exp = if (withMultipliers) experience * this.bpExperienceMultiplier else experience

    val gainEvent = PlayerBPExpGainEvent(this, pass, exp, !withMultipliers)
    Bukkit.getPluginManager().callEvent(gainEvent)

    if (gainEvent.isCancelled) {
        return
    }

    this.giveExactBPExperience(pass, gainEvent.getAmount())
}

fun Player.giveExactBPExperience(pass: BattlePass, experience: Double) {
    if (!experience.isFinite() || experience <= 0.0) {
        // Previously abs() turned a negative into a gain, hiding the misconfiguration.
        plugin.logger.warning("Refused a non-positive BP xp grant of $experience for ${pass.id}")
        return
    }

    val startLevel = this.getTier(pass)
    val change = LevelProgression.progress(pass.curve, startLevel, this.getPassExp(pass), experience)

    if (change.stopReason == StopReason.INVALID_REQUIREMENT) {
        pass.warnBrokenCurveOnce(startLevel + 1)
    }

    val gained = change.levelsGained

    if (gained == null) {
        this.setPassExp(pass, change.newXp)
        return
    }

    // Fire per level, committing as we go. Nothing is written before the event that can veto
    // it, so a cancellation leaves the player exactly where they were.
    var committed = startLevel

    for (level in gained) {
        val event = PlayerTierLevelUpEvent(this, pass, level)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled) {
            break
        }

        this.setTier(pass, level)
        committed = level
    }

    if (committed == change.newLevel) {
        this.setPassExp(pass, change.newXp)
    } else {
        // A cancelled tier stops the climb. Bank the XP that was not spent on the tiers we
        // did not grant, so the player does not lose progress to another plugin's veto.
        var refunded = change.newXp
        for (level in (committed + 1)..change.newLevel) {
            val cost = pass.curve.xpToReach(level)

            // Every level in this range was affordable a moment ago, so its cost is finite.
            // Guard anyway: banking an infinite value would persist Infinity to the player
            // profile and render as "Infinity" in every placeholder from then on.
            if (!cost.isFinite()) {
                break
            }

            refunded += cost
        }
        this.setPassExp(pass, refunded)
    }
}

fun Player.giveExactBPTiers(pass: BattlePass, amount: Int) {
    repeat(amount) {
        val currentTier = this.getTier(pass)
        val nextTier = (currentTier + 1).coerceAtMost(pass.maxLevel)

        if (nextTier == currentTier || pass.getTier(nextTier) == null) {
            return
        }

        this.setTier(pass, nextTier)
        this.setPassExp(pass, 0.0) // Reset XP when tier goes up
        val levelUpEvent = PlayerTierLevelUpEvent(this, pass, nextTier)
        Bukkit.getPluginManager().callEvent(levelUpEvent)
        if (levelUpEvent.isCancelled) {
            this.setTier(pass, currentTier)
            return
        }
    }
}

fun Player.hasReceivedTier(pass: BattlePass, tier: Int): ReceivedTierState {
    val bpTier = pass.getTier(tier) ?: return ReceivedTierState.NOT_RECEIVED
    val receivedTiers = this.getReceivedTiers(pass)

    val hasFull = bpTier.saveId in receivedTiers
    val hasFree = bpTier.saveIdFree in receivedTiers
    val hasPremium = bpTier.saveIdPremium in receivedTiers

    return when {
        hasFull -> ReceivedTierState.RECEIVED
        hasFree && hasPremium -> ReceivedTierState.RECEIVED
        hasFree -> {
            if (bpTier.rewards.any { it.tier == TierType.PREMIUM }) {
                ReceivedTierState.RECEIVED_FREE
            } else {
                ReceivedTierState.RECEIVED
            }
        }

        hasPremium -> {
            if (bpTier.rewards.any { it.tier == TierType.FREE }) {
                ReceivedTierState.RECEIVED_PREMIUM
            } else {
                ReceivedTierState.RECEIVED
            }
        }

        else -> ReceivedTierState.NOT_RECEIVED
    }
}
