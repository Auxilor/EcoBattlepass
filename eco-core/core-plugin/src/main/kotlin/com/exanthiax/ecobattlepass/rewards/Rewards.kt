package com.exanthiax.ecobattlepass.rewards

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.RegistrableCategory

object Rewards : RegistrableCategory<Reward>("reward", "rewards") {
    override fun acceptConfig(plugin: LibreforgePlugin, id: String, config: Config) {
        registry.register(
            Reward(id, config)
        )
    }

    override fun clear(plugin: LibreforgePlugin) {
        registry.clear()
    }

//    override fun afterReload(plugin: LibreforgePlugin) {
//        BattlePassLegacy.update()
//    }
}