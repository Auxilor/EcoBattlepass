@file:Suppress("DEPRECATION")

package com.exanthiax.ecobattlepass.gui

import com.exanthiax.ecobattlepass.categories.Category
import com.exanthiax.ecobattlepass.plugin
import com.exanthiax.ecobattlepass.quests.ActiveBattleQuest
import com.exanthiax.ecobattlepass.utils.InternalPlaceholders
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.gui.slot.Slot
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import org.bukkit.ChatColor
import org.bukkit.entity.Player

class QuestsGUI(
    private val player: Player, val category: Category, val page: Int = 1,
    val wasBack: Boolean = false
) {
    fun open() {
        val pattern = plugin.configYml.getStrings("quests-gui.mask.pattern")
        val menu = Menu.builder(pattern.size)
            .setTitle(
                plugin.configYml.getFormattedString("quests-gui.title")
                    .replace("%page%", page.toString())
                    .replace("%category%", ChatColor.stripColor(category.title) ?: category.id)
                    .replace("%pass%", category.battlepass.name)
            )
        var row = 1
        var num = ((page - 1) * getPerPage())
        pattern.forEach {
            var col = 1
            it.toCharArray().forEach { s ->
                kotlin.run {
                    if (s.equals('q', true)) {
                        if (num < category.quests.size) {
                            menu.setSlot(row, col, slot(category.quests[num]))
                        }
                        num++
                    }
                }
                col++
            }
            row++
        }
        menu.setMask(
            FillerMask(
                MaskItems.fromItemNames(plugin.configYml.getStrings("quests-gui.mask.items")),
                *pattern.toTypedArray()
            )
        )
        menu.setSlot(
            plugin.configYml.getInt("quests-gui.buttons.next-page.row"),
            plugin.configYml.getInt("quests-gui.buttons.next-page.column"),
            nextSlot()
        )
        menu.setSlot(
            plugin.configYml.getInt("quests-gui.buttons.prev-page.row"),
            plugin.configYml.getInt("quests-gui.buttons.prev-page.column"),
            prevSlot()
        )
        for (slotConfig in plugin.configYml.getSubsections("quests-gui.buttons.custom-slots")) {
            val resolved = slotConfig.clone().apply {
                fun r(s: String) = InternalPlaceholders.CategoryPlaceholders.replace(
                    s,
                    player = player,
                    category = category
                )

                set("item", r(getString("item")))
                set("lore", getStrings("lore").map(::r))
                listOf("left-click", "right-click", "shift-left-click", "shift-right-click").forEach { click ->
                    if (this.has(click)) {
                        this.set(click, this.getStrings(click).map(::r))
                    }
                }
            }

            menu.setSlot(
                resolved.getInt("row"),
                resolved.getInt("column"),
                ConfigSlot(resolved)
            )
        }
        if (plugin.configYml.getBool("quests-gui.buttons.close.enabled")) {
            menu.setSlot(
                plugin.configYml.getInt("quests-gui.buttons.close.row"),
                plugin.configYml.getInt("quests-gui.buttons.close.column"),
                Slot.builder(
                    ItemStackBuilder(
                        Items.lookup(plugin.configYml.getString("quests-gui.buttons.close.material"))
                    ).setDisplayName(plugin.configYml.getString("quests-gui.buttons.close.name"))
                        .addLoreLines(plugin.configYml.getFormattedStrings("quests-gui.buttons.close.lore"))
                        .build()
                ).onLeftClick { event, _ ->
                    event.whoClicked.closeInventory()
                }.build()
            )
        }
        menu.build().open(player)
    }

    private fun getPerPage(): Int {
        return plugin.configYml.getStrings("quests-gui.mask.pattern")
            .sumOf {
                it.toCharArray().filter { it1 -> it1.equals('q', true) }.size
            }
    }

    private fun getMaxPages(): Int {
        val total = category.quests.size
        val perPage = getPerPage()
        if (perPage <= 0) return 1  // safety
        return (total + perPage - 1) / perPage
    }

    private fun nextSlot(): Slot {
        val nextActive = page < getMaxPages()
        val builder = Slot.builder(
            ItemStackBuilder(
                Items.lookup(plugin.configYml.getString("quests-gui.buttons.next-page.item.${getActive(nextActive)}"))
            ).addLoreLines(
                plugin.configYml.getFormattedStrings("quests-gui.buttons.next-page.lore.${getActive(nextActive)}")
            ).build()
        )

        if (nextActive) {
            builder.onLeftClick { _, _ ->
                QuestsGUI(player, category, page + 1, wasBack = wasBack).open()
            }
        }
        return builder.build()
    }

    private fun prevSlot(): Slot {
        val prevActive = page > 1 || wasBack
        val builder = Slot.builder(
            ItemStackBuilder(
                Items.lookup(plugin.configYml.getString("quests-gui.buttons.prev-page.item.${getActive(prevActive)}"))
            ).addLoreLines(
                plugin.configYml.getFormattedStrings("quests-gui.buttons.prev-page.lore.${getActive(prevActive)}")
            ).build()
        )

        if (prevActive) {
            builder.onLeftClick { _, _ ->
                when {
                    page > 1 -> QuestsGUI(player, category, page - 1, wasBack = wasBack).open()
                    wasBack -> CategoriesGUI(player, category.battlepass, backButton = wasBack).open()
                }
            }
        }
        return builder.build()
    }

    private fun getActive(active: Boolean): String {
        return if (active) "active" else "inactive"
    }

    private fun slot(pair: ActiveBattleQuest): Slot {
        val itemBuilder = ItemStackBuilder(
            Items.lookup(pair.parent.itemString.replace("%player%", player.name)).item.clone()
        ).setDisplayName(
            pair.getFormattedName(player)
        ).addLoreLines(
            pair.getFormattedLore(player)
        )

        return Slot.builder(
            itemBuilder.build()
        )
            .build()
    }
}