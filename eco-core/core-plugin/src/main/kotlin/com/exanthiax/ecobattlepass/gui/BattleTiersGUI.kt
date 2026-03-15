package com.exanthiax.ecobattlepass.gui

import com.exanthiax.ecobattlepass.api.getTier
import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.gui.components.BattleTierComponent
import com.exanthiax.ecobattlepass.plugin
import com.exanthiax.ecobattlepass.utils.InternalPlaceholders
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.menu.MenuLayer
import com.willfp.eco.core.gui.page.PageChanger
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.util.formatEco
import org.bukkit.entity.Player

object BattleTiersGUI {
    fun createAndOpen(player: Player, pass: BattlePass, backButton: Boolean = false) {
        val maskPattern = plugin.configYml.getStrings("tiers-gui.mask.pattern").toTypedArray()
        val maskItems = MaskItems.fromItemNames(plugin.configYml.getStrings("tiers-gui.mask.materials"))

        val levelComponent = BattleTierComponent(plugin, pass)

        val menu = menu(maskPattern.size) {
            title = plugin.configYml.getString("tiers-gui.title")
                .replace("%pass%", pass.name)
                .formatEco()

            maxPages(levelComponent.pages)

            setMask(FillerMask(maskItems, *maskPattern))

            addComponent(1, 1, levelComponent)

            defaultPage {
                levelComponent.getPageOf(it.getTier(pass)).coerceAtLeast(1)
            }

            // Instead of the page changer, this will show up when on the first page
            if (backButton) {
                addComponent(
                    MenuLayer.LOWER,
                    plugin.configYml.getInt("tiers-gui.buttons.prev-page.location.row"),
                    plugin.configYml.getInt("tiers-gui.buttons.prev-page.location.column"),
                    slot(
                        ItemStackBuilder(Items.lookup(plugin.configYml.getString("tiers-gui.buttons.prev-page.material")))
                            .setDisplayName(plugin.configYml.getString("tiers-gui.buttons.prev-page.name"))
                            .build()
                    ) {
                        onLeftClick { _, _ ->
                            BattlePassGUI.createAndOpen(player, pass) }
                    }
                )
            }

            addComponent(
                plugin.configYml.getInt("tiers-gui.buttons.prev-page.location.row"),
                plugin.configYml.getInt("tiers-gui.buttons.prev-page.location.column"),
                PageChanger(
                    ItemStackBuilder(Items.lookup(plugin.configYml.getString("tiers-gui.buttons.prev-page.material")))
                        .setDisplayName(plugin.configYml.getString("tiers-gui.buttons.prev-page.name"))
                        .build(),
                    PageChanger.Direction.BACKWARDS
                )
            )

            addComponent(
                plugin.configYml.getInt("tiers-gui.buttons.next-page.location.row"),
                plugin.configYml.getInt("tiers-gui.buttons.next-page.location.column"),
                PageChanger(
                    ItemStackBuilder(Items.lookup(plugin.configYml.getString("tiers-gui.buttons.next-page.material")))
                        .setDisplayName(plugin.configYml.getString("tiers-gui.buttons.next-page.name"))
                        .build(),
                    PageChanger.Direction.FORWARDS
                )
            )

            if (plugin.configYml.getBool("tiers-gui.buttons.close.enabled"))
                setSlot(
                    plugin.configYml.getInt("tiers-gui.buttons.close.location.row"),
                    plugin.configYml.getInt("tiers-gui.buttons.close.location.column"),
                    slot(
                        ItemStackBuilder(Items.lookup(plugin.configYml.getString("tiers-gui.buttons.close.material")))
                            .setDisplayName(plugin.configYml.getString("tiers-gui.buttons.close.name"))
                            .build()
                    ) {
                        onLeftClick { event, _ ->
                            event.whoClicked.closeInventory() }
                    }
                )

            for (slotConfig in plugin.configYml.getSubsections("tiers-gui.buttons.custom-slots")) {
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

                setSlot(
                    resolved.getInt("row"),
                    resolved.getInt("column"),
                    ConfigSlot(resolved)
                )
            }
        }

        menu.open(player)
    }
}