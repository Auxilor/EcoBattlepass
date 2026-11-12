package com.exanthiax.ecobattlepass

import com.exanthiax.ecobattlepass.api.hasPremium
import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.exanthiax.ecobattlepass.categories.Categories
import com.exanthiax.ecobattlepass.commands.ecoBattlePassCommand
import com.exanthiax.ecobattlepass.config.CategoriesYml
import com.exanthiax.ecobattlepass.gui.BattleTiersGUI
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
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import wiki.ecohub.config.category.NativeConfigCategory
import wiki.ecohub.core.command.EcoCommand
import wiki.ecohub.core.plugin.EcoPlugin
import wiki.ecohub.provider.metrics.EcoMetricsChart
import wiki.ecohub.rules.condition.Conditions
import wiki.ecohub.rules.effect.Effects
import wiki.ecohub.rules.filter.Filters
import wiki.ecohub.rules.trigger.Triggers

internal lateinit var plugin: EcoBattlePass
    private set

class EcoBattlePass : EcoPlugin() {
    val categoriesYml = CategoriesYml(this)

    init {
        plugin = this
        this.getConfigHandler().addConfig(categoriesYml)
    }

    override fun loadConfigCategories(): List<NativeConfigCategory> {
        return listOf(
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

        BattleTiersGUI.onReload()
    }

    override fun handleReload() {
        BattlePasses.updateTaskBindings()
        BattleTiersGUI.onReload()
    }

    override fun createTasks() {
        this.scheduler.async().runTimer(delay = 1L, repeat = 100L) {
            Categories.values().forEach { category -> if (category.isToReset()) category.reset() }
            BattlePasses.tickUpdates()
        }
    }

    override fun loadListeners(): List<Listener> {
        return listOf(
            BattlePassListener
        )
    }

    override fun loadPluginCommands(): List<EcoCommand> {
        return listOf(
            ecoBattlePassCommand()
        )
    }

    override fun getCustomCharts() = listOf(
        EcoMetricsChart.SingleLine("total_battlepasses") { BattlePasses.values().size },
        EcoMetricsChart.SingleLine("total_quests") { BattleQuests.values().size },
        EcoMetricsChart.SingleLine("total_tasks") { BattleTasks.values().size },
        EcoMetricsChart.SingleLine("total_rewards") { Rewards.values().size },
        EcoMetricsChart.SingleLine("total_categories") { Categories.values().size },
        EcoMetricsChart.AdvancedPie("uses_premium") {
            val passes = BattlePasses.values()
            val online = Bukkit.getOnlinePlayers()
            val premium = online.count { player -> passes.any { pass -> player.hasPremium(pass) } }
            mapOf("Premium" to premium, "Free" to (online.size - premium))
        }
    )
}
