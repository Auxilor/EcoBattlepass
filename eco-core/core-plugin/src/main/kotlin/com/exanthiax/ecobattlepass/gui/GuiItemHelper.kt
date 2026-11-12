package com.exanthiax.ecobattlepass.gui

import com.exanthiax.ecobattlepass.plugin
import org.bukkit.inventory.ItemStack
import wiki.ecohub.game.item.Items
import wiki.ecohub.game.item.builder.ItemStackBuilder

object GuiItemHelper {

    fun readItem(basePath: String, state: String): ItemStack {
        // 1. Item string: try item.<state>, fallback to material
        val itemString = plugin.configYml.getStringOrNull("$basePath.item.$state")
            ?: plugin.configYml.getStringOrNull("$basePath.material")
            ?: "stone"

        val builder = ItemStackBuilder(Items.lookup(itemString))

        // 2. Name: try name.<state>, fallback to name (single string)
        //    If neither exists, the inline name from Items.lookup() is kept
        val nameFromState = plugin.configYml.getStringOrNull("$basePath.name.$state")
        val nameFromSingle = plugin.configYml.getStringOrNull("$basePath.name")
        val name = nameFromState ?: nameFromSingle
        if (name != null) {
            builder.setDisplayName(name)
        }

        // 3. Lore: try lore.<state>, fallback to lore (single list)
        val lorePath = if (plugin.configYml.has("$basePath.lore.$state")) {
            "$basePath.lore.$state"
        } else if (plugin.configYml.has("$basePath.lore")) {
            "$basePath.lore"
        } else null

        if (lorePath != null) {
            val lore = plugin.configYml.getStrings(lorePath)
            if (lore.isNotEmpty()) {
                builder.addLoreLines(lore)
            }
        }

        return builder.build()
    }
}
