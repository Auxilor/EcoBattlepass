package com.exanthiax.ecobattlepass.gui

import com.exanthiax.ecobattlepass.api.getTier
import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.gui.components.BattleTierComponent
import com.exanthiax.ecobattlepass.gui.components.EmptyDisplayMode
import com.exanthiax.ecobattlepass.gui.components.LayoutMode
import com.exanthiax.ecobattlepass.plugin
import com.exanthiax.ecobattlepass.tiers.TierType
import com.exanthiax.ecobattlepass.utils.InternalPlaceholders
import com.willfp.eco.core.gui.addCustomSlot
import com.willfp.eco.core.gui.addPageChanger
import com.willfp.eco.core.gui.component.ConfigButton
import com.willfp.eco.core.gui.component.addComponent
import com.willfp.eco.core.gui.component.closeButton
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.menu.MenuLayer
import com.willfp.eco.core.gui.page.PageChanger
import com.willfp.eco.core.gui.setMask
import com.willfp.eco.core.sound.PlayableSound
import com.willfp.eco.util.formatEco
import org.bukkit.entity.Player

object BattleTiersGUI {
    @Volatile
    lateinit var layoutMode: LayoutMode
        private set

    @Volatile
    lateinit var emptyDisplayMode: EmptyDisplayMode
        private set

    @Volatile
    var openAtCurrentTier: Boolean = true
        private set

    @Volatile
    var maxItemAmount: Int = 64
        private set

    fun onReload() {
        layoutMode = LayoutMode.fromConfig(
            plugin.configYml.getStringOrNull("tiers-gui.layout")
        )
        emptyDisplayMode = EmptyDisplayMode.fromConfig(
            plugin.configYml.getStringOrNull("tiers-gui.empty-tier-display-mode")
        )
        openAtCurrentTier = if (plugin.configYml.has("tiers-gui.open-at-current-tier")) {
            plugin.configYml.getBool("tiers-gui.open-at-current-tier")
        } else {
            true
        }
        maxItemAmount = if (plugin.configYml.has("tiers-gui.buttons.max-item-amount")) {
            val raw = plugin.configYml.getInt("tiers-gui.buttons.max-item-amount")
            if (raw in 1..99) {
                raw
            } else {
                plugin.logger.warning(
                    "Invalid tiers-gui.buttons.max-item-amount: $raw. " +
                            "Must be between 1 and 99. Defaulting to 64."
                )
                64
            }
        } else {
            64
        }
    }

    fun createAndOpen(player: Player, pass: BattlePass, backButton: Boolean = false) {
        val maskPattern = plugin.configYml.getStrings("tiers-gui.mask.pattern").toTypedArray()

        fun String.withBattlePassPlaceholders(): String =
            InternalPlaceholders.BattlePassPlaceholders.replace(this, battlepass = pass, player = player)

        val pageChangeSound = PlayableSound.create(plugin.configYml.getSubsection("sound.page-turn"))

        val components: List<BattleTierComponent>
        val totalPages: Int

        when (layoutMode) {
            LayoutMode.SPLIT -> {
                val freeComponent = BattleTierComponent(
                    plugin,
                    pass,
                    tierType = TierType.FREE,
                    patternPath = "tiers-gui.layouts.split.free-pattern",
                    legacyPatternPath = "tiers-gui.split.free-pattern",
                    emptyTierDisplayMode = emptyDisplayMode,
                    maxItemAmount = maxItemAmount
                )
                val premiumComponent = BattleTierComponent(
                    plugin,
                    pass,
                    tierType = TierType.PREMIUM,
                    patternPath = "tiers-gui.layouts.split.premium-pattern",
                    legacyPatternPath = "tiers-gui.split.premium-pattern",
                    emptyTierDisplayMode = emptyDisplayMode,
                    maxItemAmount = maxItemAmount
                )
                components = listOf(freeComponent, premiumComponent)
                totalPages = maxOf(freeComponent.pages, premiumComponent.pages)
            }

            LayoutMode.COMBINED -> {
                val levelComponent = BattleTierComponent(
                    plugin,
                    pass,
                    emptyTierDisplayMode = emptyDisplayMode,
                    maxItemAmount = maxItemAmount
                )
                components = listOf(levelComponent)
                totalPages = levelComponent.pages
            }
        }

        val prevPagePath = "tiers-gui.buttons.prev-page"
        val nextPagePath = "tiers-gui.buttons.next-page"

        val rawTitle = plugin.configYml.getString("tiers-gui.title")
            .withBattlePassPlaceholders()
            .replace("%pass%", pass.name)
            .formatEco()

        val menu = menu(maskPattern.size) {
            title = rawTitle

            maxPages(totalPages)

            setMask(plugin.configYml, "tiers-gui")

            components.forEach { addComponent(1, 1, it) }

            if (openAtCurrentTier) {
                defaultPage {
                    components.first().getPageOf(it.getTier(pass)).coerceAtLeast(1)
                }
            }

            if (backButton) {
                addComponent(
                    ConfigButton(
                        plugin.configYml.getSubsection(prevPagePath),
                        state = "active",
                        layer = MenuLayer.LOWER,
                        format = { it.withBattlePassPlaceholders() }
                    ) { _, _, _, _ ->
                        BattlePassGUI.createAndOpen(player, pass)
                    }
                )
            }

            addPageChanger(
                plugin.configYml,
                prevPagePath,
                PageChanger.Direction.BACKWARDS,
                pageChangeSound,
                format = { it.withBattlePassPlaceholders() },
                hideInactive = backButton
            )

            addPageChanger(
                plugin.configYml,
                nextPagePath,
                PageChanger.Direction.FORWARDS,
                pageChangeSound,
                format = { it.withBattlePassPlaceholders() }
            )

            addComponent(
                closeButton(
                    plugin.configYml.getSubsection("tiers-gui.buttons.close"),
                    defaultItem = "barrier",
                    enabledByDefault = false,
                    format = { it.withBattlePassPlaceholders() }
                )
            )

            for (slotConfig in plugin.configYml.getSubsections("tiers-gui.buttons.custom-slots")) {
                val resolved = slotConfig.clone().apply {
                    val itemStr = getString("item").withBattlePassPlaceholders()
                    val nameStr = getStringOrNull("name")
                    if (nameStr != null && !itemStr.lowercase().contains("name:")) {
                        set("item", "$itemStr name:\"${nameStr.withBattlePassPlaceholders()}\"")
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
        }

        menu.open(player)
    }
}
