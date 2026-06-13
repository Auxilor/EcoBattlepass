package com.exanthiax.ecobattlepass.gui

import com.exanthiax.ecobattlepass.api.getTier
import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.gui.components.BattleTierComponent
import com.exanthiax.ecobattlepass.gui.components.EmptyDisplayMode
import com.exanthiax.ecobattlepass.gui.components.LayoutMode
import com.exanthiax.ecobattlepass.plugin
import com.exanthiax.ecobattlepass.tiers.TierType
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
import com.willfp.eco.core.sound.PlayableSound
import com.willfp.eco.util.formatEco
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object BattleTiersGUI {

    lateinit var layoutMode: LayoutMode
        private set

    lateinit var emptyDisplayMode: EmptyDisplayMode
        private set

    var openAtCurrentTier: Boolean = true
        private set

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
        val maskItems = MaskItems.fromItemNames(plugin.configYml.getStrings("tiers-gui.mask.materials"))

        fun String.withBattlePassPlaceholders(): String =
            InternalPlaceholders.BattlePassPlaceholders.replace(this, battlepass = pass, player = player)

        fun pageButtonItem(basePath: String, state: String, page: Int, maxPage: Int): ItemStack? {
            val itemString = plugin.configYml.getStringOrNull("$basePath.item.$state")
                ?: plugin.configYml.getStringOrNull("$basePath.item")
                ?: plugin.configYml.getStringOrNull("$basePath.material")
                ?: return null

            val builder = ItemStackBuilder(
                Items.lookup(itemString.withBattlePassPlaceholders().withPagePlaceholders(page, maxPage))
            )

            val name = plugin.configYml.getStringOrNull("$basePath.name.$state")
                ?: plugin.configYml.getStringOrNull("$basePath.name")
            if (name != null) {
                builder.setDisplayName(name.withBattlePassPlaceholders().withPagePlaceholders(page, maxPage))
            }

            val lore = plugin.configYml.getStringsOrNull("$basePath.lore.$state")
                ?: plugin.configYml.getStringsOrNull("$basePath.lore")
                ?: emptyList()
            builder.addLoreLines(lore.map { it.withBattlePassPlaceholders().withPagePlaceholders(page, maxPage) })

            return builder.build()
        }

        val pageChangeSound = PlayableSound.create(plugin.configYml.getSubsection("sound.page-turn"))

        val components: List<BattleTierComponent>
        val totalPages: Int

        when (layoutMode) {
            LayoutMode.SPLIT -> {
                val freeComponent = BattleTierComponent(
                    plugin, pass,
                    tierType = TierType.FREE,
                    patternPath = "tiers-gui.layouts.split.free-pattern",
                    legacyPatternPath = "tiers-gui.split.free-pattern",
                    emptyTierDisplayMode = emptyDisplayMode,
                    maxItemAmount = maxItemAmount
                )
                val premiumComponent = BattleTierComponent(
                    plugin, pass,
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
                    plugin, pass,
                    emptyTierDisplayMode = emptyDisplayMode,
                    maxItemAmount = maxItemAmount
                )
                components = listOf(levelComponent)
                totalPages = levelComponent.pages
            }
        }

        val defaultPageNum = if (openAtCurrentTier) {
            components.first().getPageOf(player.getTier(pass)).coerceAtLeast(1)
        } else {
            1
        }

        val prevPagePath = "tiers-gui.buttons.prev-page"
        val nextPagePath = "tiers-gui.buttons.next-page"

        val prevRow = plugin.configYml.getInt("$prevPagePath.location.row")
        val prevCol = plugin.configYml.getInt("$prevPagePath.location.column")
        val nextRow = plugin.configYml.getInt("$nextPagePath.location.row")
        val nextCol = plugin.configYml.getInt("$nextPagePath.location.column")

        val rawTitle = plugin.configYml.getString("tiers-gui.title")
            .withBattlePassPlaceholders()
            .replace("%pass%", pass.name)
            .formatEco()

        val menu = menu(maskPattern.size) {
            title = rawTitle.withPagePlaceholders(defaultPageNum, totalPages)

            maxPages(totalPages)

            setMask(FillerMask(maskItems, *maskPattern))

            components.forEach { addComponent(1, 1, it) }

            if (openAtCurrentTier) {
                defaultPage {
                    components.first().getPageOf(it.getTier(pass)).coerceAtLeast(1)
                }
            }

            onRender { eventPlayer, eventMenu ->
                eventMenu.refreshPageTitle(eventPlayer, rawTitle, totalPages)
            }

            if (backButton) {
                addComponent(
                    MenuLayer.LOWER,
                    prevRow, prevCol,
                    slot(pageButtonItem(prevPagePath, "active", 1, totalPages) ?: Items.lookup("stone").item) {
                        onLeftClick { _, _ ->
                            BattlePassGUI.createAndOpen(player, pass)
                        }
                    }
                )
            }

            addComponent(
                MenuLayer.TOP,
                prevRow, prevCol,
                PageChangerComponent(PageChanger.Direction.BACKWARDS, pageChangeSound) { state, page, max ->
                    if (state == PageButtonState.INACTIVE && backButton) return@PageChangerComponent null
                    pageButtonItem(prevPagePath, if (state == PageButtonState.ACTIVE) "active" else "inactive", page, max)
                }
            )

            addComponent(
                MenuLayer.TOP,
                nextRow, nextCol,
                PageChangerComponent(PageChanger.Direction.FORWARDS, pageChangeSound) { state, page, max ->
                    pageButtonItem(nextPagePath, if (state == PageButtonState.ACTIVE) "active" else "inactive", page, max)
                }
            )

            if (plugin.configYml.getBool("tiers-gui.buttons.close.enabled")) {
                val closePath = "tiers-gui.buttons.close"
                val closeMaterial = plugin.configYml.getStringOrNull("$closePath.material") ?: "barrier"
                val closeName = plugin.configYml.getStringOrNull("$closePath.name")
                val closeBuilder = ItemStackBuilder(Items.lookup(closeMaterial.withBattlePassPlaceholders()))
                if (closeName != null) closeBuilder.setDisplayName(closeName.withBattlePassPlaceholders())

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
