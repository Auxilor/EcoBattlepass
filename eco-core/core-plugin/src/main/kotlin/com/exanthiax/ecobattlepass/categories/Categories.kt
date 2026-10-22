package com.exanthiax.ecobattlepass.categories

import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.LegacyLocation
import com.willfp.libreforge.loader.configs.RegistrableCategory

object Categories: RegistrableCategory<Category>("categories", "categories") {
    override val legacyLocation = LegacyLocation("categories.yml", "categories")

    override fun acceptConfig(plugin: LibreforgePlugin, id: String, config: Config) {
        val battlepassId = config.getString("battlepass")

        if (BattlePasses.getByID(battlepassId) == null) {
            plugin.logger.warning("Category $id has an unknown battlepass '$battlepassId', skipping it.")
            return
        }

        registry.register(
            Category(id, config)
        )
    }

    override fun clear(plugin: LibreforgePlugin) {
        for (category in registry) {
            for (quest in category.quests) {
                for (task in quest.tasks) {
                    task.unbind()
                }
            }
        }
        registry.clear()
    }

    fun valuesSorted(): Set<Category> {
        return this.values().sortedBy { if (it.isActive) 1 else 0 }.toSet()
    }

    override fun beforeReload(plugin: LibreforgePlugin) {
        for (category in registry) {
            for (quest in category.quests) {
                for (task in quest.tasks) {
                    task.unbind()
                }
            }
        }
    }
}