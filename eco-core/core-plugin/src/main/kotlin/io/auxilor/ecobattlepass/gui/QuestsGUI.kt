@file:Suppress("DEPRECATION")

package io.auxilor.ecobattlepass.gui

import io.auxilor.ecobattlepass.categories.Category
import io.auxilor.ecobattlepass.plugin
import io.auxilor.ecobattlepass.quests.ActiveBattleQuest
import io.auxilor.ecobattlepass.utils.InternalPlaceholders
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
    private fun String.withCategoryPlaceholders(): String =
        InternalPlaceholders.CategoryPlaceholders.replace(this, category = category, player = player)

    private fun List<String>.withCategoryPlaceholders(): List<String> =
        InternalPlaceholders.CategoryPlaceholders.replaceAll(this, category = category, player = player)

    fun open() {
        val pattern = plugin.configYml.getStrings("quests-gui.mask.pattern")
        val menu = Menu.builder(pattern.size)
            .setTitle(
                plugin.configYml.getString("quests-gui.title")
                    .replace("%page%", page.toString())
                    .replace("%category%", ChatColor.stripColor(category.title) ?: category.id)
                    .replace("%pass%", category.battlepass.name)
                    .withCategoryPlaceholders()
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
                val nameKey = getStringOrNull("name")
                val itemStr = getString("item").withCategoryPlaceholders()
                if (nameKey != null && !itemStr.contains("name:")) {
                    set("item", "$itemStr name:\"${nameKey.withCategoryPlaceholders()}\"")
                } else {
                    set("item", itemStr)
                }
                set("lore", getStrings("lore").map { it.withCategoryPlaceholders() })
                listOf("left-click", "right-click", "shift-left-click", "shift-right-click").forEach { click ->
                    if (this.has(click)) {
                        this.set(click, this.getStrings(click).map { it.withCategoryPlaceholders() })
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
                buildCloseSlot("quests-gui.buttons.close")
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
        if (perPage <= 0) return 1
        return (total + perPage - 1) / perPage
    }

    private fun nextSlot(): Slot {
        val nextActive = page < getMaxPages()
        val state = if (nextActive) "active" else "inactive"
        val builder = Slot.builder(
            buildPageItem("quests-gui.buttons.next-page", state)
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
        val state = if (prevActive) "active" else "inactive"
        val builder = Slot.builder(
            buildPageItem("quests-gui.buttons.prev-page", state)
        )

        if (prevActive) {
            builder.onLeftClick { _, _ ->
                when {
                    page > 1 -> QuestsGUI(player, category, page - 1, wasBack = wasBack).open()
                    else -> CategoriesGUI(
                        player,
                        category.battlepass,
                        backButton = true
                    ).open()
                }
            }
        }
        return builder.build()
    }

    private fun buildPageItem(basePath: String, state: String): org.bukkit.inventory.ItemStack {
        val itemString = plugin.configYml.getStringOrNull("$basePath.item.$state")
            ?: plugin.configYml.getStringOrNull("$basePath.item")
            ?: plugin.configYml.getString("$basePath.material")

        val itemBuilder = ItemStackBuilder(Items.lookup(itemString.withCategoryPlaceholders()))

        val name = plugin.configYml.getStringOrNull("$basePath.name.$state")
            ?: plugin.configYml.getStringOrNull("$basePath.name")
        if (name != null) {
            itemBuilder.setDisplayName(name.withCategoryPlaceholders())
        }

        val lore = plugin.configYml.getStringsOrNull("$basePath.lore.$state")
            ?: plugin.configYml.getStringsOrNull("$basePath.lore")
            ?: emptyList()
        itemBuilder.addLoreLines(lore.withCategoryPlaceholders())

        return itemBuilder.build()
    }

    private fun buildCloseSlot(basePath: String): Slot {
        val itemString = plugin.configYml.getStringOrNull("$basePath.item")
            ?: plugin.configYml.getString("$basePath.material")

        val itemBuilder = ItemStackBuilder(Items.lookup(itemString.withCategoryPlaceholders()))

        plugin.configYml.getStringOrNull("$basePath.name")?.let {
            itemBuilder.setDisplayName(it.withCategoryPlaceholders())
        }

        val lore = plugin.configYml.getStringsOrNull("$basePath.lore")
            ?: emptyList()
        itemBuilder.addLoreLines(lore.withCategoryPlaceholders())

        return Slot.builder(itemBuilder.build())
            .onLeftClick { event, _ ->
                event.whoClicked.closeInventory()
            }.build()
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
