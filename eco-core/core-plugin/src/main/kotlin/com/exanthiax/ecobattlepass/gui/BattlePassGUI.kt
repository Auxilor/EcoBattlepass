package com.exanthiax.ecobattlepass.gui

import com.exanthiax.ecobattlepass.api.getTier
import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.plugin
import com.exanthiax.ecobattlepass.tiers.BPTier
import com.exanthiax.ecobattlepass.utils.InternalPlaceholders
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.sound.PlayableSound
import com.willfp.eco.util.formatEco
import org.bukkit.entity.Player

object BattlePassGUI {
    fun createAndOpen(player: Player, pass: BattlePass) {
        val maskPattern = plugin.configYml.getStrings("battlepass-gui.mask.pattern").toTypedArray()
        val maskItems = MaskItems.fromItemNames(plugin.configYml.getStrings("battlepass-gui.mask.materials"))
        val level = player.getTier(pass)

        fun String.withBattlePassPlaceholders(): String =
            InternalPlaceholders.BattlePassPlaceholders.replace(this, battlepass = pass, player = player)

        fun List<String>.withBattlePassPlaceholders(): List<String> =
            InternalPlaceholders.BattlePassPlaceholders.replaceAll(this, battlepass = pass, player = player)

        val menu = menu(maskPattern.size) {
            title = plugin.configYml.getString("battlepass-gui.title")
                .withBattlePassPlaceholders()
                .replace("%pass%", pass.name)
                .formatEco()

            setMask(FillerMask(maskItems, *maskPattern))

            // Tiers button — supports item OR material, + name
            setSlot(
                plugin.configYml.getInt("battlepass-gui.buttons.tiers.location.row"),
                plugin.configYml.getInt("battlepass-gui.buttons.tiers.location.column"),
                slot(
                    run {
                        val itemString = (plugin.configYml.getStringOrNull("battlepass-gui.buttons.tiers.item")
                            ?: plugin.configYml.getString("battlepass-gui.buttons.tiers.material"))
                            .withBattlePassPlaceholders()
                        val builder = ItemStackBuilder(Items.lookup(itemString))
                        plugin.configYml.getStringOrNull("battlepass-gui.buttons.tiers.name")?.let {
                            builder.setDisplayName(it.withBattlePassPlaceholders())
                        }
                        builder.addLoreLines(
                            BPTier(level, pass).format(
                                plugin.configYml.getStrings("battlepass-gui.buttons.tiers.lore"),
                                player
                            )
                        )
                        builder.build()
                    }
                ) {
                    onLeftClick { _, _ ->
                        PlayableSound.create(plugin.configYml.getSubsection("sound.gui-click-sound"))?.playTo(player)
                        BattleTiersGUI.createAndOpen(player, pass, true)
                    }
                }
            )

            // Quests button — supports item OR material, + name
            setSlot(
                plugin.configYml.getInt("battlepass-gui.buttons.quests.location.row"),
                plugin.configYml.getInt("battlepass-gui.buttons.quests.location.column"),
                slot(
                    run {
                        val itemString = (plugin.configYml.getStringOrNull("battlepass-gui.buttons.quests.item")
                            ?: plugin.configYml.getString("battlepass-gui.buttons.quests.material"))
                            .withBattlePassPlaceholders()
                        val builder = ItemStackBuilder(Items.lookup(itemString))
                        plugin.configYml.getStringOrNull("battlepass-gui.buttons.quests.name")?.let {
                            builder.setDisplayName(it.withBattlePassPlaceholders())
                        }
                        builder.addLoreLines(
                            plugin.configYml.getStrings("battlepass-gui.buttons.quests.lore")
                                .withBattlePassPlaceholders()
                        )
                        builder.build()
                    }
                ) {
                    onLeftClick { _, _ ->
                        PlayableSound.create(plugin.configYml.getSubsection("sound.gui-click-sound"))?.playTo(player)
                        CategoriesGUI(player, pass, backButton = true).open()
                    }
                }
            )

            // Close button — supports item OR material, + name
            if (plugin.configYml.getBool("battlepass-gui.buttons.close.enabled")) {
                val closePath = "battlepass-gui.buttons.close"
                val closeItemString = (plugin.configYml.getStringOrNull("$closePath.item")
                    ?: plugin.configYml.getString("$closePath.material"))
                    .withBattlePassPlaceholders()
                val closeBuilder = ItemStackBuilder(Items.lookup(closeItemString))
                plugin.configYml.getStringOrNull("$closePath.name")?.let {
                    closeBuilder.setDisplayName(it.withBattlePassPlaceholders())
                }
                val closeLore = plugin.configYml.getStringsOrNull("$closePath.lore")
                    ?: emptyList()
                closeBuilder.addLoreLines(closeLore.withBattlePassPlaceholders())

                setSlot(
                    plugin.configYml.getInt("$closePath.location.row"),
                    plugin.configYml.getInt("$closePath.location.column"),
                    slot(closeBuilder.build()) {
                        onLeftClick { event, _ ->
                            event.whoClicked.closeInventory()
                        }
                    }
                )
            }

            // Custom slots — supports name as separate key
            for (slotConfig in plugin.configYml.getSubsections("battlepass-gui.buttons.custom-slots")) {
                val resolved = slotConfig.clone().apply {
                    val nameKey = getStringOrNull("name")
                    val itemStr = getString("item").withBattlePassPlaceholders()
                    val formattedName = nameKey?.withBattlePassPlaceholders()?.let { " name:\"$it\"" } ?: ""
                    val finalItemStr = if (!itemStr.contains("name:")) "$itemStr$formattedName" else itemStr

                    set("item", finalItemStr)
                    set("lore", getStrings("lore").map { it.withBattlePassPlaceholders() })
                    listOf("left-click", "right-click", "shift-left-click", "shift-right-click").forEach { click ->
                        if (this.has(click)) {
                            this.set(click, this.getStrings(click).map { it.withBattlePassPlaceholders() })
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
