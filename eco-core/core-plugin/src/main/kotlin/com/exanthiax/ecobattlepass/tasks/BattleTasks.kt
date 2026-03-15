package com.exanthiax.ecobattlepass.tasks

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.RegistrableCategory

object BattleTasks : RegistrableCategory<BattleTask>("task", "tasks") {
    override fun acceptConfig(plugin: LibreforgePlugin, id: String, config: Config) {
        registry.register(BattleTask(id, config))
    }

    override fun clear(plugin: LibreforgePlugin) {
        registry.clear()
    }
}