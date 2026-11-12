package com.exanthiax.ecobattlepass.quests

import wiki.ecohub.config.Config
import wiki.ecohub.config.category.RegistrableCategory
import wiki.ecohub.core.plugin.EcoPlugin

object BattleQuests : RegistrableCategory<BattleQuest>("quests", "quests") {
    override fun acceptConfig(plugin: LibreforgePlugin, id: String, config: Config) {
        registry.register(
            BattleQuest(id, config)
        )
    }

    override fun clear(plugin: LibreforgePlugin) {
        registry.clear()
    }
}