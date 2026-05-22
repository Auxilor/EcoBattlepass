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
import com.willfp.eco.core.gui.onEvent
import com.willfp.eco.core.gui.page.PageChangeEvent
import com.willfp.eco.core.gui.page.PageChanger
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
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

        /**
         * Reads a button item supporting both old and new config formats:
         *
         * Old format:
         *   material: orange_stained_glass_pane
         *   name: "&aNext page"
         *
         * New format:
         *   item:
         *     active: orange_stained_glass_pane
         *     inactive: gray_stained_glass_pane
         *   name:
         *     active: "&aNext page"
         *     inactive: "&7No more pages"
         */
        fun readButtonItem(basePath: String, state: String): ItemStack {
            // Try new format: item.active / item.inactive
            val itemString = plugin.configYml.getStringOrNull("$basePath.item.$state")

            if (itemString != null) {
                val nameString = plugin.configYml.getStringOrNull("$basePath.name.$state")
                    ?: plugin.configYml.getStringOrNull("$basePath.name")
                val builder = ItemStackBuilder(Items.lookup(itemString.withBattlePassPlaceholders()))
                if (nameString != null) builder.setDisplayName(nameString.withBattlePassPlaceholders())
                return builder.build()
            }

            // Old format: material with optional inline name
            val materialString = plugin.configYml.getStringOrNull("$basePath.material") ?: "stone"
            val nameString = plugin.configYml.getStringOrNull("$basePath.name")
            val builder = ItemStackBuilder(Items.lookup(materialString.withBattlePassPlaceholders()))
            if (nameString != null) builder.setDisplayName(nameString.withBattlePassPlaceholders())
            return builder.build()
        }

        val components: List<BattleTierComponent>
        val totalPages: Int

        when (layoutMode) {
            LayoutMode.SPLIT -> {
                val freeComponent = BattleTierComponent(
                    plugin, pass,
                    tierType = TierType.FREE,
                    patternPath = "tiers-gui.split.free-pattern",
                    emptyTierDisplayMode = emptyDisplayMode,
                    maxItemAmount = maxItemAmount
                )
                val premiumComponent = BattleTierComponent(
                    plugin, pass,
                    tierType = TierType.PREMIUM,
                    patternPath = "tiers-gui.split.premium-pattern",
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

        // Calculate default page for initial title (%page% placeholder)
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

        val menu = menu(maskPattern.size) {
            title = plugin.configYml.getString("tiers-gui.title")
                .withBattlePassPlaceholders()
                .replace("%pass%", pass.name)
                .replace("%page%", defaultPageNum.toString())
                .replace("%max_page%", totalPages.toString())
                .formatEco()

            maxPages(totalPages)

            setMask(FillerMask(maskItems, *maskPattern))

            components.forEach { addComponent(1, 1, it) }

            // Open at current tier (configurable, default true)
            if (openAtCurrentTier) {
                defaultPage {
                    components.first().getPageOf(it.getTier(pass)).coerceAtLeast(1)
                }
            }

            // Update title on page change for %page% / %max_page% (Paper 1.20+ only)
            onEvent<PageChangeEvent> { eventPlayer, _, event ->
                try {
                    val newTitle = plugin.configYml.getString("tiers-gui.title")
                        .withBattlePassPlaceholders()
                        .replace("%pass%", pass.name)
                        .replace("%page%", event.newPage.toString())
                        .replace("%max_page%", totalPages.toString())
                        .formatEco()
                    @Suppress("DEPRECATION")
                    eventPlayer.openInventory.setTitle(newTitle)
                } catch (_: Exception) {
                    // setTitle not available on this server version
                }
            }

            // --- Prev-page ---

            // Inactive item (shows when PageChanger returns null on first page)
            val prevInactiveItem = plugin.configYml.getStringOrNull("$prevPagePath.item.inactive")
            if (prevInactiveItem != null && !backButton) {
                val inactiveName = plugin.configYml.getStringOrNull("$prevPagePath.name.inactive")
                val inactiveBuilder = ItemStackBuilder(Items.lookup(prevInactiveItem.withBattlePassPlaceholders()))
                if (inactiveName != null) inactiveBuilder.setDisplayName(inactiveName.withBattlePassPlaceholders())
                addComponent(
                    MenuLayer.LOWER,
                    prevRow, prevCol,
                    slot(inactiveBuilder.build())
                )
            }

            // Back button (shows on first page instead of prev-page when opened from BattlePassGUI)
            if (backButton) {
                addComponent(
                    MenuLayer.LOWER,
                    prevRow, prevCol,
                    slot(readButtonItem(prevPagePath, "active")) {
                        onLeftClick { _, _ ->
                            BattlePassGUI.createAndOpen(player, pass)
                        }
                    }
                )
            }

            // Prev-page changer (MIDDLE layer — covers inactive/back button when on page 2+)
            addComponent(
                prevRow, prevCol,
                PageChanger(
                    readButtonItem(prevPagePath, "active"),
                    PageChanger.Direction.BACKWARDS
                )
            )

            // --- Next-page ---

            // Inactive item (shows when PageChanger returns null on last page)
            val nextInactiveItem = plugin.configYml.getStringOrNull("$nextPagePath.item.inactive")
            if (nextInactiveItem != null) {
                val inactiveName = plugin.configYml.getStringOrNull("$nextPagePath.name.inactive")
                val inactiveBuilder = ItemStackBuilder(Items.lookup(nextInactiveItem.withBattlePassPlaceholders()))
                if (inactiveName != null) inactiveBuilder.setDisplayName(inactiveName.withBattlePassPlaceholders())
                addComponent(
                    MenuLayer.LOWER,
                    nextRow, nextCol,
                    slot(inactiveBuilder.build())
                )
            }

            // Next-page changer
            addComponent(
                nextRow, nextCol,
                PageChanger(
                    readButtonItem(nextPagePath, "active"),
                    PageChanger.Direction.FORWARDS
                )
            )

            // --- Close button ---
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

            // --- Custom slots ---
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
