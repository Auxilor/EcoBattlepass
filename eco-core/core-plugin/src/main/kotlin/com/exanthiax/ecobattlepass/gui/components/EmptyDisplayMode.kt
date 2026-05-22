package com.exanthiax.ecobattlepass.gui.components

import com.exanthiax.ecobattlepass.plugin

enum class EmptyDisplayMode(val configKey: String) {
    NORMAL("normal"),
    HIDDEN("hidden"),
    HIDDEN_BEHIND_LEVEL("hidden-behind-level"),
    BEHIND_LEVEL("behind-level"),
    ALL_CLAIMED("all");

    companion object {
        private val BY_KEY = entries.associateBy { it.configKey }

        fun fromConfig(value: String?): EmptyDisplayMode {
            if (value == null) return NORMAL

            val mode = BY_KEY[value.lowercase()]
            if (mode != null) return mode

            plugin.logger.warning(
                "Invalid empty-tier-display-mode: '$value'. " +
                        "Defaulting to NORMAL. Valid options: ${BY_KEY.keys.joinToString()}"
            )
            return NORMAL
        }
    }
}
