package com.exanthiax.ecobattlepass

import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.exanthiax.ecobattlepass.categories.Categories
import com.exanthiax.ecobattlepass.commands.EcoBattlePassCommand
import com.exanthiax.ecobattlepass.libreforge.conditions.ConditionHasBPPremium
import com.exanthiax.ecobattlepass.libreforge.conditions.ConditionHasBPTier
import com.exanthiax.ecobattlepass.libreforge.effects.EffectBPExpMultiplier
import com.exanthiax.ecobattlepass.libreforge.effects.EffectGiveBPExp
import com.exanthiax.ecobattlepass.libreforge.effects.EffectGiveBPTier
import com.exanthiax.ecobattlepass.libreforge.effects.EffectGiveTaskExp
import com.exanthiax.ecobattlepass.libreforge.effects.EffectSetBPTier
import com.exanthiax.ecobattlepass.libreforge.effects.EffectTaskExpMultiplier
import com.exanthiax.ecobattlepass.libreforge.filters.FilterReward
import com.exanthiax.ecobattlepass.libreforge.filters.FilterTask
import com.exanthiax.ecobattlepass.libreforge.triggers.TriggerBPExpGain
import com.exanthiax.ecobattlepass.libreforge.triggers.TriggerBPRewardClaim
import com.exanthiax.ecobattlepass.libreforge.triggers.TriggerBPTaskComplete
import com.exanthiax.ecobattlepass.libreforge.triggers.TriggerBPTierUp
import com.exanthiax.ecobattlepass.quests.BattleQuests
import com.exanthiax.ecobattlepass.rewards.Rewards
import com.exanthiax.ecobattlepass.tasks.BattleTasks
import com.exanthiax.ecobattlepass.utils.BattlePassListener
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.config.BaseConfig
import com.willfp.eco.core.config.ConfigType
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.effects.Effects
import com.willfp.libreforge.filters.Filters
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import com.willfp.libreforge.triggers.Triggers
import org.bukkit.event.Listener

lateinit var plugin: EcoBattlePass
    private set

class EcoBattlePass : LibreforgePlugin() {
    init {
        plugin = this
        this.configHandler.addConfig(
            object : BaseConfig(
                "categories",
                this,
                false,
                ConfigType.YAML
            ) {}
        )
    }

    override fun loadListeners(): List<Listener> {
        return listOf(
            BattlePassListener
        )
    }

    override fun loadPluginCommands(): MutableList<PluginCommand> {
        return mutableListOf(
            EcoBattlePassCommand
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