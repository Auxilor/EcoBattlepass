package com.exanthiax.ecobattlepass.libreforge.filters

import com.exanthiax.ecobattlepass.api.events.PlayerPostRewardEvent
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.util.containsIgnoreCase
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.filters.Filter
import com.willfp.libreforge.triggers.TriggerData

object FilterReward: Filter<NoCompileData, Collection<String>>("battlepass_reward") {
    override fun getValue(config: Config, data: TriggerData?, key: String): Collection<String> {
        return config.getStrings(key)
    }

    override fun isMet(data: TriggerData, value: Collection<String>, compileData: NoCompileData): Boolean {
        val event = data.event as? PlayerPostRewardEvent ?: return false

        return value.containsIgnoreCase(event.reward.id)
    }
}