package com.exanthiax.ecobattlepass.rewards

import wiki.ecohub.config.Config
import wiki.ecohub.config.category.RegistrableCategory
import wiki.ecohub.core.plugin.EcoPlugin

object Rewards : RegistrableCategory<Reward>("reward", "rewards") {
    override fun acceptConfig(plugin: EcoPlugin, id: String, config: Config) {
        registry.register(
            Reward(id, config)
        )
    }

    override fun clear(plugin: EcoPlugin) {
        registry.clear()
    }
}