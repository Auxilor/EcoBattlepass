package com.exanthiax.ecobattlepass.quests

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.RegistrableCategory

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