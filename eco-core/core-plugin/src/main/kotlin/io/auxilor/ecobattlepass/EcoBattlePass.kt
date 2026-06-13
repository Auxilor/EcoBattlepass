package io.auxilor.ecobattlepass

import io.auxilor.ecobattlepass.api.hasPremium
import io.auxilor.ecobattlepass.battlepass.BattlePasses
import io.auxilor.ecobattlepass.categories.Categories
import io.auxilor.ecobattlepass.commands.EcoBattlePassCommand
import io.auxilor.ecobattlepass.libreforge.conditions.ConditionHasBPPremium
import io.auxilor.ecobattlepass.libreforge.conditions.ConditionHasBPTier
import io.auxilor.ecobattlepass.libreforge.effects.EffectBPExpMultiplier
import io.auxilor.ecobattlepass.libreforge.effects.EffectGiveBPExp
import io.auxilor.ecobattlepass.libreforge.effects.EffectGiveBPTier
import io.auxilor.ecobattlepass.libreforge.effects.EffectGiveTaskExp
import io.auxilor.ecobattlepass.libreforge.effects.EffectSetBPTier
import io.auxilor.ecobattlepass.libreforge.effects.EffectTaskExpMultiplier
import io.auxilor.ecobattlepass.libreforge.filters.FilterReward
import io.auxilor.ecobattlepass.libreforge.filters.FilterTask
import io.auxilor.ecobattlepass.gui.BattleTiersGUI
import io.auxilor.ecobattlepass.libreforge.triggers.TriggerBPExpGain
import io.auxilor.ecobattlepass.libreforge.triggers.TriggerBPRewardClaim
import io.auxilor.ecobattlepass.libreforge.triggers.TriggerBPTaskComplete
import io.auxilor.ecobattlepass.libreforge.triggers.TriggerBPTierUp
import io.auxilor.ecobattlepass.quests.BattleQuests
import io.auxilor.ecobattlepass.rewards.Rewards
import io.auxilor.ecobattlepass.tasks.BattleTasks
import io.auxilor.ecobattlepass.utils.BattlePassListener
import com.willfp.eco.core.bstats.EcoMetricsChart
import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.Bukkit
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

        BattleTiersGUI.onReload()
    }

    override fun handleReload() {
        // BattlePassLegacy.update()
        BattlePasses.updateTaskBindings()
        BattleTiersGUI.onReload()
    }

    override fun createTasks() {
        this.scheduler.runAsyncTimer(1L, 100L) {
            Categories.values().forEach { category -> if (category.isToReset()) category.reset() }
            BattlePasses.tickUpdates()
        }
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
