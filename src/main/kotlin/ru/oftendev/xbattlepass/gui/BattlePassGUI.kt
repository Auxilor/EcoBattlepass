package ru.oftendev.xbattlepass.gui

import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.util.formatEco
import org.bukkit.entity.Player
import ru.oftendev.xbattlepass.plugin

object BattlePassGUI {
    fun createAndOpen(player: Player) {
        val maskPattern = plugin.configYml.getStrings("battlepass-gui.mask.pattern").toTypedArray()
        val maskItems = MaskItems.fromItemNames(plugin.configYml.getStrings("battlepass-gui.mask.materials"))

        val menu = menu(maskPattern.size) {
            title = plugin.configYml.getString("battlepass-gui.title").formatEco()

            setMask(
                FillerMask(
                    maskItems,
                    *maskPattern
                )
            )

            setSlot(
                plugin.configYml.getInt("battlepass-gui.buttons.tiers.location.row"),
                plugin.configYml.getInt("battlepass-gui.buttons.tiers.location.column"),
                slot(
                    ItemStackBuilder(Items.lookup(plugin.configYml.getString("battlepass-gui.buttons.tiers.item")))
                        .setDisplayName(plugin.configYml.getString("battlepass-gui.buttons.tiers.name"))
                        .addLoreLines(
                            plugin.configYml.getStrings("battlepass-gui.buttons.tiers.lore")
                        )
                        .build()
                ) {
                    onLeftClick { _, _ ->
                        BattleTiersGUI.createAndOpen(player, true)
                    }
                }
            )

            setSlot(
                plugin.configYml.getInt("battlepass-gui.buttons.quests.location.row"),
                plugin.configYml.getInt("battlepass-gui.buttons.quests.location.column"),
                slot(
                    ItemStackBuilder(Items.lookup(plugin.configYml.getString("battlepass-gui.buttons.quests.item")))
                        .setDisplayName(plugin.configYml.getString("battlepass-gui.buttons.quests.name"))
                        .addLoreLines(
                            plugin.configYml.getStrings("battlepass-gui.buttons.quests.lore")
                        )
                        .build()
                ) {
                    onLeftClick { _, _ ->
                        CategoriesGUI(player, backButton = true).open()
                    }
                }
            )

            for (config in plugin.configYml.getSubsections("battlepass-gui.buttons.custom-slots")) {
                setSlot(
                    config.getInt("row"),
                    config.getInt("column"),
                    ConfigSlot(config)
                )
            }
        }

        menu.open(player)
    }
}