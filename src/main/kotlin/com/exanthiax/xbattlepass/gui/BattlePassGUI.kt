package com.exanthiax.xbattlepass.gui

import com.exanthiax.xbattlepass.api.getTier
import com.exanthiax.xbattlepass.battlepass.BattlePass
import com.exanthiax.xbattlepass.plugin
import com.exanthiax.xbattlepass.tiers.BPTier
import com.exanthiax.xbattlepass.utils.SoundUtils
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.util.formatEco
import org.bukkit.entity.Player

object BattlePassGUI {
    fun createAndOpen(player: Player, pass: BattlePass) {
        val maskPattern = plugin.configYml.getStrings("battlepass-gui.mask.pattern").toTypedArray()
        val maskItems = MaskItems.fromItemNames(plugin.configYml.getStrings("battlepass-gui.mask.materials"))
        val level = player.getTier(pass)

        val menu = menu(maskPattern.size) {
            title = plugin.configYml.getString("battlepass-gui.title")
                .replace("%pass%", pass.name)
                .formatEco()

            setMask(FillerMask(maskItems, *maskPattern))

            setSlot(
                plugin.configYml.getInt("battlepass-gui.buttons.tiers.location.row"),
                plugin.configYml.getInt("battlepass-gui.buttons.tiers.location.column"),
                slot(
                    ItemStackBuilder(Items.lookup(plugin.configYml.getString("battlepass-gui.buttons.tiers.item")))
                        .setDisplayName(plugin.configYml.getString("battlepass-gui.buttons.tiers.name"))
                        .addLoreLines(
                            BPTier(level, pass)
                                .format(
                                    plugin.configYml.getStrings("battlepass-gui.buttons.tiers.lore"),
                                    player
                                )
                        )
                        .build()
                ) {
                    onLeftClick { _, _ ->
                        SoundUtils.playIfEnabled(player, "sound.gui-click-sound")
                        BattleTiersGUI.createAndOpen(player, pass, true)
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
                        SoundUtils.playIfEnabled(player, "sound.gui-click-sound")
                        CategoriesGUI(player, pass, backButton = true).open()
                    }
                }
            )

            if (plugin.configYml.getBool("battlepass-gui.buttons.close.enabled")) {
                setSlot(
                    plugin.configYml.getInt("battlepass-gui.buttons.close.location.row"),
                    plugin.configYml.getInt("battlepass-gui.buttons.close.location.column"),
                    slot(
                        ItemStackBuilder(
                            Items.lookup(plugin.configYml.getString("battlepass-gui.buttons.close.material"))
                        ).setDisplayName(plugin.configYml.getString("battlepass-gui.buttons.close.name"))
                            .addLoreLines(plugin.configYml.getFormattedStrings("battlepass-gui.buttons.close.lore"))
                            .build()
                    ) {
                        onLeftClick { event, _ ->
                            event.whoClicked.closeInventory()
                        }
                    }
                )
            }

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