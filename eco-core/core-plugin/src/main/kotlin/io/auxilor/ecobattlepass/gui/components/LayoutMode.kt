package io.auxilor.ecobattlepass.gui.components

import io.auxilor.ecobattlepass.plugin

enum class LayoutMode(val configKey: String) {
    COMBINED("combined"),
    SPLIT("split");

    companion object {
        private val BY_KEY = entries.associateBy { it.configKey }

        fun fromConfig(value: String?): LayoutMode {
            if (value == null) return COMBINED

            val mode = BY_KEY[value.lowercase()]
            if (mode != null) return mode

            plugin.logger.warning(
                "Invalid tiers-gui layout: '$value'. " +
                        "Defaulting to COMBINED. Valid options: ${BY_KEY.keys.joinToString()}"
            )
            return COMBINED
        }
    }
}
