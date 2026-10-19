package com.exanthiax.ecobattlepass.gui

import com.exanthiax.ecobattlepass.categories.Category
import com.exanthiax.ecobattlepass.plugin
import com.exanthiax.ecobattlepass.quests.ActiveBattleQuest
import com.exanthiax.ecobattlepass.utils.InternalPlaceholders
import com.willfp.eco.core.gui.addCustomSlot
import com.willfp.eco.core.gui.addPage
import com.willfp.eco.core.gui.addPageChanger
import com.willfp.eco.core.gui.component.ConfigButton
import com.willfp.eco.core.gui.component.addComponent
import com.willfp.eco.core.gui.component.closeButton
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

class QuestsGUI(
    private val player: Player, val category: Category,
    val wasBack: Boolean = false
) {
    private fun String.withCategoryPlaceholders(): String =
        InternalPlaceholders.CategoryPlaceholders.replace(this, category = category, player = player)

    fun open() {
        val pattern = plugin.configYml.getStrings("quests-gui.mask.pattern")
        val perPage = getPerPage()
        val maxPage = getMaxPages()

        @Suppress("DEPRECATION")
        val strippedCategoryTitle = org.bukkit.ChatColor.stripColor(category.title)

        val rawTitle = plugin.configYml.getString("quests-gui.title")
            .replace("%category%", strippedCategoryTitle ?: category.id)
            .replace("%pass%", category.battlepass.name)
            .withCategoryPlaceholders()
            .formatEco()

        val pageChangeSound = PlayableSound.create(plugin.configYml.getSubsection("sound.page-turn"))

        val prevPagePath = "quests-gui.buttons.prev-page"
        val nextPagePath = "quests-gui.buttons.next-page"

        val menu = menu(pattern.size) {
            title = rawTitle

            maxPages(maxPage)

            addPageChanger(
                plugin.configYml,
                prevPagePath,
                PageChanger.Direction.BACKWARDS,
                pageChangeSound,
                format = { it.withCategoryPlaceholders() },
                hideInactive = wasBack
            )

            addPageChanger(
                plugin.configYml,
                nextPagePath,
                PageChanger.Direction.FORWARDS,
                pageChangeSound,
                format = { it.withCategoryPlaceholders() }
            )

            for (page in 1..maxPage) {
                addPage(page) {
                    setMask(plugin.configYml, "quests-gui")

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
                        addComponent(
                            ConfigButton(
                                plugin.configYml.getSubsection(prevPagePath),
                                state = "active",
                                layer = MenuLayer.LOWER,
                                format = { it.withCategoryPlaceholders() }
                            ) { _, _, _, _ ->
                                CategoriesGUI(player, category.battlepass, backButton = true).open()
                            }
                        )
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

                        addCustomSlot(resolved)
                    }

                    addComponent(
                        closeButton(
                            plugin.configYml.getSubsection("quests-gui.buttons.close"),
                            enabledByDefault = false,
                            format = { it.withCategoryPlaceholders() }
                        )
                    )
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