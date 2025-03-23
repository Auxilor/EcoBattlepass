package ru.oftendev.xbattlepass.gui

import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.gui.slot.Slot
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import ru.oftendev.xbattlepass.categories.Categories
import ru.oftendev.xbattlepass.categories.Category
import ru.oftendev.xbattlepass.plugin
import ru.oftendev.xbattlepass.quests.ActiveBattleQuest

class QuestsGUI(private val player: Player, val category: Category, val page: Int = 1) {
    fun open() {
        val pattern = plugin.configYml.getStrings("quests-gui.mask.pattern")
        val menu = Menu.builder(pattern.size)
            .setTitle(plugin.configYml.getFormattedString("quests-gui.title")
                .replace("%page%", page.toString())
                .replace("%category%", ChatColor.stripColor(category.name) ?: category.id)
            )
        var row = 1
        var num = ((page-1)*getPerPage())
        pattern.forEach {
            var col = 1
            it.toCharArray().forEach {
                    s -> kotlin.run {
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
            plugin.configYml.getInt("quests-gui.next-page.row"),
            plugin.configYml.getInt("quests-gui.next-page.column"),
            nextSlot()
        )
        menu.setSlot(
            plugin.configYml.getInt("quests-gui.prev-page.row"),
            plugin.configYml.getInt("quests-gui.prev-page.column"),
            prevSlot()
        )
        for (config in plugin.configYml.getSubsections("quests-gui.custom-slots")) {
            menu.setSlot(
                config.getInt("row"),
                config.getInt("column"),
                ConfigSlot(config)
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
        return total/getPerPage()
    }

    private fun nextSlot(): Slot {
        val nextActive = page < getMaxPages()
        val builder = Slot.builder(
            ItemStackBuilder(
                Items.lookup(plugin.configYml.getString("quests-gui.next-page.item.${getActive(nextActive)}"))
            ).addLoreLines(
                plugin.configYml.getFormattedStrings("quests-gui.next-page.lore.${getActive(nextActive)}")
            ).build()
        )
        if (nextActive) {
            builder.onLeftClick { _, _ ->
                run {
                    QuestsGUI(player, category, page + 1).open()
                }
            }
        }
        return builder.build()
    }

    private fun prevSlot(): Slot {
        val builder = Slot.builder(
            ItemStackBuilder(
                Items.lookup(plugin.configYml.getString("quests-gui.prev-page.item.${getActive(true)}"))
            ).addLoreLines(
                plugin.configYml.getFormattedStrings("quests-gui.prev-page.lore.${getActive(true)}")
            ).build()
        )
        builder.onLeftClick { _, _ ->
            run {
                if (page > 1) {
                    QuestsGUI(player, category, page - 1).open()
                } else {
                    CategoriesGUI(player).open()
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
            pair.parent.item.item.clone()
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