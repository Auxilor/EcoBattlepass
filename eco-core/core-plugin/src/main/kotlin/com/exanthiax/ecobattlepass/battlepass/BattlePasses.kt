package com.exanthiax.ecobattlepass.battlepass

import com.exanthiax.ecobattlepass.categories.Categories
import com.exanthiax.ecobattlepass.plugin
import com.exanthiax.ecobattlepass.tasks.ActiveBattleTask
import com.exanthiax.ecobattlepass.tiers.TierType
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.RegistrableCategory

object BattlePasses: RegistrableCategory<BattlePass>("battlepasses", "battlepasses") {
    val activeTasks: List<ActiveBattleTask>
        get() = Categories.values().filter { it.isActive }
            .map { category -> category.quests.map { it.tasks }.flatten() }.flatten()

    override fun acceptConfig(plugin: LibreforgePlugin, id: String, config: Config) {
        registry.register(
            BattlePass(id, config)
        )
    }

    override fun clear(plugin: LibreforgePlugin) {
        registry.clear()
    }

    fun getRewardsFormat(tierType: TierType): String {
        val key = when (tierType) {
            TierType.PREMIUM -> "premium"
            TierType.FREE -> "free"
        }

        return plugin.configYml.getFormattedString("tiers-gui.buttons.$key-rewards-format")
    }

    fun tickUpdates() {
        for (category in Categories.values()) {
            if (category.isActive && !category.consideredActive) {
                updateTaskBindings()
            } else if (!category.isActive && category.consideredActive) {
                updateTaskBindings()
            }
        }
    }

    fun updateTaskBindings() {
        Categories.values().forEach { category -> category.quests.forEach { quest -> quest.tasks.forEach {
            it.unbind()
        } } }

        activeTasks.forEach { task -> task.unbind() }

        activeTasks.forEach { task -> task.bind() }

        plugin.logger.info("Rebound ${activeTasks.size} tasks")
    }
}