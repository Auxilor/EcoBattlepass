package com.exanthiax.ecobattlepass.libreforge.filters

import com.exanthiax.ecobattlepass.api.events.PlayerTaskCompleteEvent
import wiki.ecohub.config.ArgType
import wiki.ecohub.config.Config
import wiki.ecohub.core.registry.NoCompileData
import wiki.ecohub.rules.filter.Filter
import wiki.ecohub.rules.trigger.TriggerData
import wiki.ecohub.ui.text.containsIgnoreCase

object FilterTask: Filter<NoCompileData, Collection<String>>("battlepass_task") {
    override val description = "Matches when the completed task's ID is in the given list."

    override val categories = setOf("meta")

    override val valueType = ArgType.STRING_LIST

    override fun getValue(config: Config, data: TriggerData?, key: String): Collection<String> {
        return config.getStrings(key)
    }

    override fun isMet(data: TriggerData, value: Collection<String>, compileData: NoCompileData): Boolean {
        val event = data.event as? PlayerTaskCompleteEvent ?: return false

        return value.containsIgnoreCase(event.task.parent.id)
    }
}