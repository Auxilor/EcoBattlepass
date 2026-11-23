package com.exanthiax.ecobattlepass.gui

import com.exanthiax.ecobattlepass.categories.Category
import com.exanthiax.ecobattlepass.plugin
import com.exanthiax.ecobattlepass.quests.ActiveBattleQuest
import com.exanthiax.ecobattlepass.utils.InternalPlaceholders
import org.bukkit.entity.Player
import wiki.ecohub.game.item.Items
import wiki.ecohub.game.item.builder.ItemStackBuilder
import wiki.ecohub.game.sound.PlayableSound
import wiki.ecohub.modules.menus.addCustomSlot
import wiki.ecohub.modules.menus.addPage
import wiki.ecohub.modules.menus.addPageChanger
import wiki.ecohub.modules.menus.component.ConfigButton
import wiki.ecohub.modules.menus.component.addComponent
import wiki.ecohub.modules.menus.component.closeButton
import wiki.ecohub.modules.menus.menu
import wiki.ecohub.modules.menus.menu.MenuLayer
import wiki.ecohub.modules.menus.page.PageChanger
import wiki.ecohub.modules.menus.setMask
import wiki.ecohub.modules.menus.slot
import wiki.ecohub.ui.text.formatEco

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
            setTitle(rawTitle)

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
                                this["item"] = "$itemStr name:\"${nameKey.withCategoryPlaceholders()}\""
                            } else {
                                this["item"] = itemStr
                            }
                            this["lore"] = getStrings("lore").map { it.withCategoryPlaceholders() }
                            listOf("left-click", "right-click", "shift-left-click", "shift-right-click").forEach { click ->
                                if (this.has(click)) {
                                    this[click] = this.getStrings(click).map { it.withCategoryPlaceholders() }
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
                Items.lookup(quest.parent.itemString.replace("%player%", player.name)).getItem().clone()
            ).setDisplayName(
                quest.getFormattedName(player)
            ).addLoreLines(
                quest.getFormattedLore(player)
            ).build()
        )
}