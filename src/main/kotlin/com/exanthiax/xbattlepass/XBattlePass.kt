package com.exanthiax.xbattlepass

import com.exanthiax.xbattlepass.battlepass.BattlePasses
import com.exanthiax.xbattlepass.categories.Categories
import com.exanthiax.xbattlepass.libreforge.conditions.ConditionHasBPPremium
import com.exanthiax.xbattlepass.libreforge.conditions.ConditionHasBPTier
import com.exanthiax.xbattlepass.libreforge.effects.EffectBPExpMultiplier
import com.exanthiax.xbattlepass.libreforge.effects.EffectGiveBPExp
import com.exanthiax.xbattlepass.libreforge.effects.EffectGiveBPTier
import com.exanthiax.xbattlepass.libreforge.effects.EffectGiveTaskExp
import com.exanthiax.xbattlepass.libreforge.effects.EffectSetBPTier
import com.exanthiax.xbattlepass.libreforge.effects.EffectTaskExpMultiplier
import com.exanthiax.xbattlepass.libreforge.filters.FilterReward
import com.exanthiax.xbattlepass.libreforge.filters.FilterTask
import com.exanthiax.xbattlepass.libreforge.triggers.TriggerBPExpGain
import com.exanthiax.xbattlepass.libreforge.triggers.TriggerBPRewardClaim
import com.exanthiax.xbattlepass.libreforge.triggers.TriggerBPTaskComplete
import com.exanthiax.xbattlepass.libreforge.triggers.TriggerBPTierUp
import com.exanthiax.xbattlepass.quests.BattleQuests
import com.exanthiax.xbattlepass.rewards.Rewards
import com.exanthiax.xbattlepass.tasks.BattleTasks
import com.exanthiax.xbattlepass.utils.BattlePassListener
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.config.BaseConfig
import com.willfp.eco.core.config.ConfigType
import com.willfp.eco.util.toNiceString
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.effects.Effects
import com.willfp.libreforge.filters.Filters
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import com.willfp.libreforge.triggers.Triggers
import org.bukkit.event.Listener

lateinit var plugin: XBattlePass
    private set

class XBattlePass: LibreforgePlugin() {
    init {
        plugin = this
        this.configHandler.addConfig(
            object: BaseConfig(
                "categories",
                this,
                false,
                ConfigType.YAML
            ) {}
        )
    }

    override fun loadListeners(): List<Listener> {
        return listOf(
            BattlePassListener(this)
        )
    }

    override fun loadPluginCommands(): MutableList<PluginCommand> {
        return mutableListOf(
            com.exanthiax.xbattlepass.commands.XBattlePassCommand
        )
    }

    override fun loadConfigCategories(): List<ConfigCategory> {
        return mutableListOf(
            Rewards,
            BattleTasks,
            BattleQuests,
            BattlePasses,
            Categories
        )
    }

    override fun handleEnable() {
        BattlePasses.updateTaskBindings()

        // Libreforge register
        Effects.register(EffectBPExpMultiplier)
        Effects.register(EffectGiveBPExp)
        Effects.register(EffectGiveBPTier)
        Effects.register(EffectGiveTaskExp)
        Effects.register(EffectSetBPTier)
        Effects.register(EffectTaskExpMultiplier)

        Conditions.register(ConditionHasBPTier)
        Conditions.register(ConditionHasBPPremium)

        Filters.register(FilterReward)
        Filters.register(FilterTask)

        Triggers.register(TriggerBPExpGain)
        Triggers.register(TriggerBPRewardClaim)
        Triggers.register(TriggerBPTaskComplete)
        Triggers.register(TriggerBPTierUp)
    }

    override fun handleReload() {
        // BattlePassLegacy.update()
        BattlePasses.updateTaskBindings()
    }

    override fun createTasks() {
        this.scheduler.runAsyncTimer(1L, 100L) {
            Categories.values().forEach { category -> if (category.isToReset()) category.reset() }
            BattlePasses.tickUpdates()
        }
    }
}

fun msToString(ms: Long): String {
    // Define constants
    val secondsPerMs = 0.001
    val secondsInMinute = 60
    val secondsInHour = 3600
    val secondsInDay = 86400

    // Convert ticks to total seconds
    val totalSeconds = ms * secondsPerMs

    // Calculate days, hours, minutes, and seconds
    val days = (totalSeconds / secondsInDay).toInt()
    val hours = ((totalSeconds % secondsInDay) / secondsInHour).toInt()
    val minutes = ((totalSeconds % secondsInHour) / secondsInMinute).toInt()
    val seconds = (totalSeconds % secondsInMinute).toInt()

    val lst = mutableListOf<String>()

    if (days > 0) {
        lst += plugin.configYml.getFormattedString("time-format.days")
            .replace("%value%", days.toNiceString())
    }
    if (hours > 0) {
        lst += plugin.configYml.getFormattedString("time-format.hours")
            .replace("%value%", hours.toNiceString())
    }
    if (minutes > 0) {
        lst += plugin.configYml.getFormattedString("time-format.minutes")
            .replace("%value%", minutes.toNiceString())
    }

    lst += plugin.configYml.getFormattedString("time-format.seconds")
        .replace("%value%", seconds.toNiceString())

    // Format the result as a string
    return lst.joinToString(plugin.configYml.getFormattedString("time-format.split"))
}