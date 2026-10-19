package com.exanthiax.ecobattlepass.gui

import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.categories.Category
import com.exanthiax.ecobattlepass.plugin
import com.exanthiax.ecobattlepass.utils.InternalPlaceholders
import com.willfp.eco.core.gui.addCustomSlot
import com.willfp.eco.core.gui.addPage
import com.willfp.eco.core.gui.addPageChanger
import com.willfp.eco.core.gui.getSlotLocations
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.menu.MenuLayer
import com.willfp.eco.core.gui.page.PageChanger
import com.willfp.eco.core.gui.setMask
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.sound.PlayableSound
import com.willfp.eco.util.formatEco
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CategoriesGUI(
    private val player: Player, private val pass: BattlePass,
    val backButton: Boolean = false
) {

    private fun String.withBattlePassPlaceholders(): String =
        InternalPlaceholders.BattlePassPlaceholders.replace(this, battlepass = pass, player = player)

    private fun List<String>.withBattlePassPlaceholders(): List<String> =
        InternalPlaceholders.BattlePassPlaceholders.replaceAll(this, battlepass = pass, player = player)

    fun open() {
        val pattern = plugin.configYml.getStrings("categories-gui.mask.pattern")
        val perPage = getPerPage()
        val maxPage = getMaxPages()
        val categories = pass.categories.toList()

        val rawTitle = plugin.configYml.getString("categories-gui.title")
            .withBattlePassPlaceholders()
            .formatEco()

        val pageChangeSound = PlayableSound.create(plugin.configYml.getSubsection("sound.page-turn"))

        val prevPagePath = "categories-gui.buttons.prev-page"
        val nextPagePath = "categories-gui.buttons.next-page"

        val menu = menu(pattern.size) {
            title = rawTitle

            maxPages(maxPage)

            buildPageItem(prevPagePath, "active")?.let { active ->
                val inactive = if (backButton) null else buildPageItem(prevPagePath, "inactive")
                for (location in plugin.configYml.getSlotLocations(prevPagePath)) {
                    addPageChanger(
                        PageChanger.Direction.BACKWARDS,
                        active,
                        inactive,
                        pageChangeSound,
                        location.row,
                        location.column
                    )
                }
            }

            buildPageItem(nextPagePath, "active")?.let { active ->
                val inactive = buildPageItem(nextPagePath, "inactive")
                for (location in plugin.configYml.getSlotLocations(nextPagePath)) {
                    addPageChanger(
                        PageChanger.Direction.FORWARDS,
                        active,
                        inactive,
                        pageChangeSound,
                        location.row,
                        location.column
                    )
                }
            }

            for (page in 1..maxPage) {
                addPage(page) {
                    setMask(plugin.configYml, "categories-gui")

                    var num = (page - 1) * perPage
                    var row = 1
                    pattern.forEach { line ->
                        var col = 1
                        line.toCharArray().forEach { s ->
                            if (s.equals('c', true)) {
                                if (num < categories.size) {
                                    setSlot(row, col, categorySlot(categories[num]))
                                }
                                num++
                            }
                            col++
                        }
                        row++
                    }

                    if (backButton) {
                        buildPageItem(prevPagePath, "active")?.let { active ->
                            val backSlot = slot(active) {
                                onLeftClick { _, _ ->
                                    BattlePassGUI.createAndOpen(player, pass)
                                }
                            }
                            for (location in plugin.configYml.getSlotLocations(prevPagePath)) {
                                addComponent(MenuLayer.LOWER, location.row, location.column, backSlot)
                            }
                        }
                    }

                    for (slotConfig in plugin.configYml.getSubsections("categories-gui.buttons.custom-slots")) {
                        val resolved = slotConfig.clone().apply {
                            val nameKey = getStringOrNull("name")
                            val itemStr = getString("item").withBattlePassPlaceholders()
                            if (nameKey != null && !itemStr.contains("name:")) {
                                set("item", "$itemStr name:\"${nameKey.withBattlePassPlaceholders()}\"")
                            } else {
                                set("item", itemStr)
                            }
                            set("lore", getStrings("lore").map { it.withBattlePassPlaceholders() })
                            listOf("left-click", "right-click", "shift-left-click", "shift-right-click").forEach { click ->
                                if (this.has(click)) {
                                    this.set(click, this.getStrings(click).map { it.withBattlePassPlaceholders() })
                                }
                            }
                        }

                        addCustomSlot(resolved)
                    }

                    if (plugin.configYml.getBool("categories-gui.buttons.close.enabled")) {
                        val closeSlot = buildCloseSlot("categories-gui.buttons.close")
                        for (location in plugin.configYml.getSlotLocations("categories-gui.buttons.close")) {
                            setSlot(location.row, location.column, closeSlot)
                        }
                    }
                }
            }
        }

        menu.open(player)
    }

    private fun getPerPage(): Int {
        return plugin.configYml.getStrings("categories-gui.mask.pattern")
            .sumOf {
                it.toCharArray().filter { it1 -> it1.equals('c', true) }.size
            }
    }

    private fun getMaxPages(): Int {
        val total = pass.categories.size
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
            Items.lookup(itemString.withBattlePassPlaceholders())
        )

        val name = plugin.configYml.getStringOrNull("$basePath.name.$state")
            ?: plugin.configYml.getStringOrNull("$basePath.name")
        if (name != null) {
            itemBuilder.setDisplayName(name.withBattlePassPlaceholders())
        }

        val lore = plugin.configYml.getStringsOrNull("$basePath.lore.$state")
            ?: plugin.configYml.getStringsOrNull("$basePath.lore")
            ?: emptyList()
        itemBuilder.addLoreLines(lore.map { it.withBattlePassPlaceholders() })

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

        val itemBuilder = ItemStackBuilder(Items.lookup(itemString.withBattlePassPlaceholders()))

        plugin.configYml.getStringOrNull("$basePath.name")?.let {
            itemBuilder.setDisplayName(it.withBattlePassPlaceholders())
        }

        val lore = plugin.configYml.getStringsOrNull("$basePath.lore")
            ?: emptyList()
        itemBuilder.addLoreLines(lore.withBattlePassPlaceholders())

        return itemBuilder.build()
    }

    private fun categorySlot(category: Category) =
        slot(category.getDisplayItem(player)) {
            onLeftClick { _, _ ->
                if (category.isActive) {
                    PlayableSound.create(plugin.configYml.getSubsection("sound.gui-click-sound"))?.playTo(player)
                    QuestsGUI(player, category, wasBack = backButton).open()
                }
            }
        }
}