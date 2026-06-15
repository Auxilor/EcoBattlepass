@file:Suppress("DEPRECATION")

package com.exanthiax.ecobattlepass.gui

import com.exanthiax.ecobattlepass.categories.Category
import com.exanthiax.ecobattlepass.plugin
import com.exanthiax.ecobattlepass.quests.ActiveBattleQuest
import com.exanthiax.ecobattlepass.utils.InternalPlaceholders
import com.willfp.eco.core.gui.addPage
import com.willfp.eco.core.gui.addPageChanger
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.menu.MenuLayer
import com.willfp.eco.core.gui.page.PageChanger
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.sound.PlayableSound
import com.willfp.eco.util.formatEco
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class QuestsGUI(
    private val player: Player, val category: Category,
    val wasBack: Boolean = false
) {
    private fun String.withCategoryPlaceholders(): String =
        InternalPlaceholders.CategoryPlaceholders.replace(this, category = category, player = player)

    private fun List<String>.withCategoryPlaceholders(): List<String> =
        InternalPlaceholders.CategoryPlaceholders.replaceAll(this, category = category, player = player)

    fun open() {
        val pattern = plugin.configYml.getStrings("quests-gui.mask.pattern")
        val perPage = getPerPage()
        val maxPage = getMaxPages()

        val rawTitle = plugin.configYml.getString("quests-gui.title")
            .replace("%category%", ChatColor.stripColor(category.title) ?: category.id)
            .replace("%pass%", category.battlepass.name)
            .withCategoryPlaceholders()
            .formatEco()

        val pageChangeSound = PlayableSound.create(plugin.configYml.getSubsection("sound.page-turn"))

        val prevPagePath = "quests-gui.buttons.prev-page"
        val nextPagePath = "quests-gui.buttons.next-page"

        fun loc(path: String, key: String): Int =
            plugin.configYml.getIntOrNull("$path.location.$key") ?: plugin.configYml.getInt("$path.$key")

        val prevRow = loc(prevPagePath, "row")
        val prevCol = loc(prevPagePath, "column")
        val nextRow = loc(nextPagePath, "row")
        val nextCol = loc(nextPagePath, "column")

        val menu = menu(pattern.size) {
            title = rawTitle

            maxPages(maxPage)

            buildPageItem(prevPagePath, "active")?.let { active ->
                val inactive = if (wasBack) null else buildPageItem(prevPagePath, "inactive")
                addPageChanger(PageChanger.Direction.BACKWARDS, active, inactive, pageChangeSound, prevRow, prevCol)
            }

            buildPageItem(nextPagePath, "active")?.let { active ->
                addPageChanger(PageChanger.Direction.FORWARDS, active, buildPageItem(nextPagePath, "inactive"), pageChangeSound, nextRow, nextCol)
            }

            for (page in 1..maxPage) {
                addPage(page) {
                    setMask(
                        FillerMask(
                            MaskItems.fromItemNames(plugin.configYml.getStrings("quests-gui.mask.materials")),
                            *pattern.toTypedArray()
                        )
                    )

                    var num = (page - 1) * perPage
                    var row = 1
                    pattern.forEach { line ->
                        var col = 1
                        line.toCharArray().forEach { s ->
                            if (s.equals('q', true)) {
                                if (num < category.quests.size) {
                                    setSlot(row, col, questSlot(category.quests[num]))
                                }
                                num++
                            }
                            col++
                        }
                        row++
                    }

                    if (wasBack) {
                        buildPageItem(prevPagePath, "active")?.let { active ->
                            addComponent(
                                MenuLayer.LOWER,
                                prevRow, prevCol,
                                slot(active) {
                                    onLeftClick { _, _ ->
                                        CategoriesGUI(player, category.battlepass, backButton = true).open()
                                    }
                                }
                            )
                        }
                    }

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

                        setSlot(
                            resolved.getInt("row"),
                            resolved.getInt("column"),
                            ConfigSlot(resolved)
                        )
                    }

                    if (plugin.configYml.getBool("quests-gui.buttons.close.enabled")) {
                        setSlot(
                            loc("quests-gui.buttons.close", "row"),
                            loc("quests-gui.buttons.close", "column"),
                            buildCloseSlot("quests-gui.buttons.close")
                        )
                    }
                }
            }
        }

        menu.open(player)
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
        return ((total + perPage - 1) / perPage).coerceAtLeast(1)
    }

    private fun buildPageItem(basePath: String, state: String): ItemStack? {
        val itemString = plugin.configYml.getStringOrNull("$basePath.item.$state")
            ?: plugin.configYml.getStringOrNull("$basePath.item")
            ?: plugin.configYml.getStringOrNull("$basePath.material")
            ?: return null

        val itemBuilder = ItemStackBuilder(
            Items.lookup(itemString.withCategoryPlaceholders())
        )

        val name = plugin.configYml.getStringOrNull("$basePath.name.$state")
            ?: plugin.configYml.getStringOrNull("$basePath.name")
        if (name != null) {
            itemBuilder.setDisplayName(name.withCategoryPlaceholders())
        }

        val lore = plugin.configYml.getStringsOrNull("$basePath.lore.$state")
            ?: plugin.configYml.getStringsOrNull("$basePath.lore")
            ?: emptyList()
        itemBuilder.addLoreLines(lore.map { it.withCategoryPlaceholders() })

        return itemBuilder.build()
    }

    private fun buildCloseSlot(basePath: String) =
        slot(buildCloseItem(basePath)) {
            onLeftClick { event, _ ->
                event.whoClicked.closeInventory()
            }
        }

    private fun buildCloseItem(basePath: String): ItemStack {
        val itemString = plugin.configYml.getStringOrNull("$basePath.item")
            ?: plugin.configYml.getString("$basePath.material")

        val itemBuilder = ItemStackBuilder(Items.lookup(itemString.withCategoryPlaceholders()))

        plugin.configYml.getStringOrNull("$basePath.name")?.let {
            itemBuilder.setDisplayName(it.withCategoryPlaceholders())
        }

        val lore = plugin.configYml.getStringsOrNull("$basePath.lore")
            ?: emptyList()
        itemBuilder.addLoreLines(lore.withCategoryPlaceholders())

        return itemBuilder.build()
    }

    private fun questSlot(quest: ActiveBattleQuest) =
        slot(
            ItemStackBuilder(
                Items.lookup(quest.parent.itemString.replace("%player%", player.name)).item.clone()
            ).setDisplayName(
                quest.getFormattedName(player)
            ).addLoreLines(
                quest.getFormattedLore(player)
            ).build()
        )
}