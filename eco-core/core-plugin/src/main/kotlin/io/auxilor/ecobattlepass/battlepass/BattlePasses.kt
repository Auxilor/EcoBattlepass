package io.auxilor.ecobattlepass.battlepass

import io.auxilor.ecobattlepass.categories.Categories
import io.auxilor.ecobattlepass.plugin
import io.auxilor.ecobattlepass.tasks.ActiveBattleTask
import io.auxilor.ecobattlepass.tiers.TierType
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
        val categories = Categories.values()

        if (categories.any { it.isActive != it.consideredActive }) {
            updateTaskBindings()
            categories.forEach { it.consideredActive = it.isActive }
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