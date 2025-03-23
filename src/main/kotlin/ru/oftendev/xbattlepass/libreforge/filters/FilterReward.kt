package ru.oftendev.xbattlepass.libreforge.filters

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.util.containsIgnoreCase
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.filters.Filter
import com.willfp.libreforge.triggers.TriggerData
import ru.oftendev.xbattlepass.api.events.PlayerRewardEvent

object FilterReward: Filter<NoCompileData, Collection<String>>("battlepass_reward") {
    override fun getValue(config: Config, data: TriggerData?, key: String): Collection<String> {
        return config.getStrings(key)
    }

    override fun isMet(data: TriggerData, value: Collection<String>, compileData: NoCompileData): Boolean {
        val event = data.event as? PlayerRewardEvent ?: return false

        return value.containsIgnoreCase(event.reward.id)
    }
}