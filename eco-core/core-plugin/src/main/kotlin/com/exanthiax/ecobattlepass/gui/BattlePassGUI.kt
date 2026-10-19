package com.exanthiax.ecobattlepass.gui

import com.exanthiax.ecobattlepass.api.getTier
import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.plugin
import com.exanthiax.ecobattlepass.tiers.BPTier
import com.exanthiax.ecobattlepass.utils.InternalPlaceholders
import com.willfp.eco.core.gui.addCustomSlot
import com.willfp.eco.core.gui.component.addComponent
import com.willfp.eco.core.gui.component.closeButton
import com.willfp.eco.core.gui.getSlotLocations
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.setMask
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.sound.PlayableSound
import com.willfp.eco.util.formatEco
import org.bukkit.entity.Player

object BattlePassGUI {
    fun createAndOpen(player: Player, pass: BattlePass) {
        val maskPattern = plugin.configYml.getStrings("battlepass-gui.mask.pattern").toTypedArray()
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

            setMask(plugin.configYml, "battlepass-gui")

            // Tiers button — supports item OR material, + name
            for (location in plugin.configYml.getSlotLocations("battlepass-gui.buttons.tiers")) {
                setSlot(
                    location.row,
                    location.column,
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
            }

            // Quests button — supports item OR material, + name
            for (location in plugin.configYml.getSlotLocations("battlepass-gui.buttons.quests")) {
                setSlot(
                    location.row,
                    location.column,
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
            }

            addComponent(
                closeButton(
                    plugin.configYml.getSubsection("battlepass-gui.buttons.close"),
                    enabledByDefault = false,
                    format = { it.withBattlePassPlaceholders() }
                )
            )

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

                addCustomSlot(resolved)
            }
        }

        menu.open(player)
    }
}
