package com.exanthiax.ecobattlepass.tasks

import wiki.ecohub.config.Config
import wiki.ecohub.config.category.RegistrableCategory
import wiki.ecohub.core.plugin.EcoPlugin

object BattleTasks : RegistrableCategory<BattleTask>("task", "tasks") {
    override fun acceptConfig(plugin: LibreforgePlugin, id: String, config: Config) {
        registry.register(BattleTask(id, config))
    }

    override fun clear(plugin: LibreforgePlugin) {
        registry.clear()
    }
}