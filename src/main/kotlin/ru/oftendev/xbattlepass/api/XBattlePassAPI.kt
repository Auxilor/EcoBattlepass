package ru.oftendev.xbattlepass.api

import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import ru.oftendev.xbattlepass.api.events.PlayerBPExpGainEvent
import ru.oftendev.xbattlepass.api.events.PlayerQuestCompleteEvent
import ru.oftendev.xbattlepass.api.events.PlayerTaskCompleteEvent
import ru.oftendev.xbattlepass.api.events.PlayerTierLevelUpEvent
import ru.oftendev.xbattlepass.battlepass.BPTier
import ru.oftendev.xbattlepass.battlepass.BattlePass
import ru.oftendev.xbattlepass.plugin
import ru.oftendev.xbattlepass.quests.ActiveBattleQuest
import ru.oftendev.xbattlepass.tasks.ActiveBattleTask
import kotlin.math.abs

var premiumPermission = plugin.battlePassYml.getString("battlepass.premium-permission")

val tierKey = PersistentDataKey(
    plugin.createNamespacedKey("bp_tier"),
    PersistentDataKeyType.INT, 0
)

val passExpKey = PersistentDataKey(
    plugin.createNamespacedKey("bp_pass_exp"),
    PersistentDataKeyType.DOUBLE, 0.0
)

fun updatePremiumPermission() {
    premiumPermission = plugin.battlePassYml.getString("battlepass.premium-permission")
}

val bpTierKey = PersistentDataKey(
    plugin.createNamespacedKey("bp_tier"),
    PersistentDataKeyType.INT, 0
)

val receivedTiersKey = PersistentDataKey(
    plugin.createNamespacedKey("bp_tiers_received"),
    PersistentDataKeyType.STRING_LIST, emptyList()
)

var Player.bpTier: Int
    get() = this.profile.read(bpTierKey)
    set(value) = this.profile.write(bpTierKey, value)

var Player.bpPassExp: Double
    get() = this.profile.read(passExpKey)
    set(value) = this.profile.write(passExpKey, value)

var Player.receivedTiers: List<String>
    get() = this.profile.read(receivedTiersKey)
    set(value) = this.profile.write(receivedTiersKey, value.distinct())

val Player.hasPremium: Boolean
    get() = this.hasPermission(premiumPermission)

fun Player.receiveTier(tier: BPTier) {
    tier.rewards.filter { it.isAllowed(this) }.forEach {
        it.reward.grant(this)
    }

    this.receivedTiers += tier.saveId
}

fun Player.hasCompletedTask(task: ActiveBattleTask): Boolean {
    return this.profile.read(task.completedKey)
}

fun Player.hasCompletedQuest(quest: ActiveBattleQuest): Boolean {
    if (this.profile.read(quest.completedKey)) {
        return true
    } else {
        if (quest.tasks.count { this.hasCompletedTask(it) } >= quest.parent.taskAmount) {
            this.profile.write(quest.completedKey, true)
            return true
        }
    }
    return false
}

fun Player.taskProgress(task: ActiveBattleTask): Double {
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

fun Player.giveBPExperience(experience: Double, withMultipliers: Boolean = true) {
    val exp = abs(
        if (withMultipliers) experience * this.bpExperienceMultiplier
        else experience
    )

    val gainEvent = PlayerBPExpGainEvent(this, exp, !withMultipliers)
    Bukkit.getPluginManager().callEvent(gainEvent)

    if (gainEvent.isCancelled) {
        return
    }

    this.giveExactBPExperience(gainEvent.getAmount())
}

fun Player.giveExactBPExperience(experience: Double) {
    val level = this.bpTier

    val progress = this.bpPassExp + experience

    if (progress >= BattlePass.getExpForLevel(level + 1)) {
        val overshoot = progress - BattlePass.getExpForLevel(level + 1)
        this.bpPassExp = 0.0
        this.bpTier += 1
        val levelUpEvent = PlayerTierLevelUpEvent(this, level + 1)
        Bukkit.getPluginManager().callEvent(levelUpEvent)
        if (!levelUpEvent.isCancelled) {
            this.giveExactBPExperience(overshoot)
        }
    } else {
        this.bpPassExp = progress
    }
}

fun Player.hasReceivedTier(tier: Int) : Boolean {
    val bpTier = BattlePass.getTier(tier) ?: return false
    return bpTier.saveId in this.receivedTiers
}