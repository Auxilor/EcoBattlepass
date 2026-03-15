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
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionAttachment
import java.util.UUID
import kotlin.math.abs

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
    tier.rewards.filter { it.isAllowed(this, tier.battlepass) }.forEach {
        it.reward.grant(this)
    }

    this.setReceivedTiers(tier.battlepass, this.getReceivedTiers(tier.battlepass)
            + if (this.hasPremium(tier.battlepass)) tier.saveId else tier.saveIdFree)
}

fun Player.receiveTierPremiumOnly(tier: BPTier) {
    tier.rewards.filter { it.tier == TierType.PREMIUM }.forEach {
        it.reward.grant(this)
    }


    this.setReceivedTiers(tier.battlepass, this.getReceivedTiers(tier.battlepass) - tier.saveIdFree + tier.saveId )
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
    val exp = abs(
        if (withMultipliers) experience * this.bpExperienceMultiplier
        else experience
    )

    val gainEvent = PlayerBPExpGainEvent(this, pass, exp, !withMultipliers)
    Bukkit.getPluginManager().callEvent(gainEvent)

    if (gainEvent.isCancelled) {
        return
    }

    this.giveExactBPExperience(pass, gainEvent.getAmount())
}

fun Player.giveExactBPExperience(pass: BattlePass, experience: Double) {
    val level = this.getTier(pass)

    val progress = this.getPassExp(pass) + experience

    if (progress >= pass.getExpForLevel(level + 1)) {
        val overshoot = progress - pass.getExpForLevel(level + 1)
        this.setPassExp(pass, 0.0)
        this.setTier(pass, level + 1)
        val levelUpEvent = PlayerTierLevelUpEvent(this, pass, level + 1)
        Bukkit.getPluginManager().callEvent(levelUpEvent)
        if (!levelUpEvent.isCancelled) {
            this.giveExactBPExperience(pass, overshoot)
        }
    } else {
        this.setPassExp(pass, progress)
    }
}

fun Player.giveExactBPTiers(pass: BattlePass, amount: Int) {
    repeat(amount) {
        val currentTier = this.getTier(pass)
        val nextTier = currentTier + 1
        if (pass.getTier(nextTier) == null) {
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

fun Player.hasReceivedTier(pass: BattlePass, tier: Int) : ReceivedTierState {
    val bpTier = pass.getTier(tier) ?: return ReceivedTierState.NOT_RECEIVED
    val receivedTiers = this.getReceivedTiers(pass)
    return if (bpTier.saveId in receivedTiers)  {
        ReceivedTierState.RECEIVED
    } else if (bpTier.saveIdFree in receivedTiers && bpTier.rewards.any { it.tier == TierType.PREMIUM }) {
        ReceivedTierState.RECEIVED_FREE
    } else if (bpTier.saveIdFree in receivedTiers) {
        ReceivedTierState.RECEIVED
    } else ReceivedTierState.NOT_RECEIVED
}