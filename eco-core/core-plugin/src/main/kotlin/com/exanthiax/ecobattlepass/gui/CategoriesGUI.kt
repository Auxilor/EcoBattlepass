package com.exanthiax.ecobattlepass.gui

import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.categories.Category
import com.exanthiax.ecobattlepass.plugin
import com.exanthiax.ecobattlepass.utils.InternalPlaceholders
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.gui.slot.Slot
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.sound.PlayableSound
import org.bukkit.entity.Player

class CategoriesGUI(private val player: Player, val pass: BattlePass,
                    val page: Int = 1, val backButton: Boolean = false) {
    fun open() {
        val pattern = plugin.configYml.getStrings("categories-gui.mask.pattern")
        val menu = Menu.builder(pattern.size)
            .setTitle(plugin.configYml.getFormattedString("categories-gui.title")
                .replace("%page%", page.toString())
                .replace("%pass%", pass.name)
            )
        var row = 1
        var num = ((page-1)*getPerPage())
        pattern.forEach {
            var col = 1
            it.toCharArray().forEach {
                    s -> kotlin.run {
                if (s.equals('c', true)) {
                    if (num < pass.categories.size) {
                        menu.setSlot(row, col, slot(pass.categories.toList()[num]))
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
                MaskItems.fromItemNames(plugin.configYml.getStrings("categories-gui.mask.items")),
                *pattern.toTypedArray()
            )
        )
        menu.setSlot(
            plugin.configYml.getInt("categories-gui.buttons.next-page.row"),
            plugin.configYml.getInt("categories-gui.buttons.next-page.column"),
            nextSlot()
        )
        menu.setSlot(
            plugin.configYml.getInt("categories-gui.buttons.prev-page.row"),
            plugin.configYml.getInt("categories-gui.buttons.prev-page.column"),
            prevSlot()
        )
        for (slotConfig in plugin.configYml.getSubsections("categories-gui.buttons.custom-slots")) {
            val resolved = slotConfig.clone().apply {
                fun r(s: String) = InternalPlaceholders.BattlePassPlaceholders.replace(
                    s,
                    player = player,
                    battlepass = pass
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

        if (plugin.configYml.getBool("categories-gui.buttons.close.enabled")) {
            menu.setSlot(
                plugin.configYml.getInt("categories-gui.buttons.close.row"),
                plugin.configYml.getInt("categories-gui.buttons.close.column"),
                Slot.builder(
                    ItemStackBuilder(
                        Items.lookup(plugin.configYml.getString("categories-gui.buttons.close.material"))
                    ).setDisplayName(plugin.configYml.getString("categories-gui.buttons.close.name"))
                        .addLoreLines(plugin.configYml.getFormattedStrings("categories-gui.buttons.close.lore"))
                        .build()
                ).onLeftClick { event, _ ->
                    event.whoClicked.closeInventory()
                }.build()
            )
        }

        menu.build().open(player)
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
        if (perPage == 0) return 1
        return (total + perPage - 1) / perPage
    }

    private fun nextSlot(): Slot {
        val nextActive = page < getMaxPages()
        val builder = Slot.builder(
            ItemStackBuilder(
                Items.lookup(plugin.configYml.getString("categories-gui.buttons.next-page.item.${getActive(nextActive)}"))
            ).addLoreLines(
                plugin.configYml.getFormattedStrings("categories-gui.buttons.next-page.lore.${getActive(nextActive)}")
            ).build()
        )
        if (nextActive) {
            builder.onLeftClick { _, _ ->
                CategoriesGUI(player, pass, page + 1, backButton).open()
            }
        }
        return builder.build()
    }

    private fun prevSlot(): Slot {
        val prevActive = page > 1 || backButton
        val builder = Slot.builder(
            ItemStackBuilder(
                Items.lookup(plugin.configYml.getString("categories-gui.buttons.prev-page.item.${getActive(prevActive)}"))
            ).addLoreLines(
                plugin.configYml.getFormattedStrings("categories-gui.buttons.prev-page.lore.${getActive(prevActive)}")
            ).build()
        )

        if (prevActive) {
            builder.onLeftClick { _, _ ->
                when {
                    page > 1 -> CategoriesGUI(player, pass, page - 1, backButton).open()
                    backButton -> BattlePassGUI.createAndOpen(player, pass)
                }
            }
        }
        return builder.build()
    }

    private fun getActive(active: Boolean): String {
        return if (active) "active" else "inactive"
    }

    private fun slot(pair: Category): Slot {
        val itemBuilder = ItemStackBuilder(pair.getDisplayItem(player))

        return Slot.builder(itemBuilder.build())
            .onLeftClick { _, _, _ ->
                if (pair.isActive) {
                    PlayableSound.create(plugin.configYml.getSubsection("sound.gui-click-sound"))?.playTo(player)
                    QuestsGUI(player, pair, wasBack = backButton).open()
                }
            }
            .build()
    }
}