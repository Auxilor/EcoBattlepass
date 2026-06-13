package io.auxilor.ecobattlepass.gui

import io.auxilor.ecobattlepass.battlepass.BattlePass
import io.auxilor.ecobattlepass.categories.Category
import io.auxilor.ecobattlepass.plugin
import io.auxilor.ecobattlepass.utils.InternalPlaceholders
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.gui.slot.Slot
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.sound.PlayableSound
import org.bukkit.entity.Player

class CategoriesGUI(
    private val player: Player, val pass: BattlePass,
    val page: Int = 1, val backButton: Boolean = false
) {

    private fun String.withBattlePassPlaceholders(): String =
        InternalPlaceholders.BattlePassPlaceholders.replace(this, battlepass = pass, player = player)

    private fun List<String>.withBattlePassPlaceholders(): List<String> =
        InternalPlaceholders.BattlePassPlaceholders.replaceAll(this, battlepass = pass, player = player)

    fun open() {
        val pattern = plugin.configYml.getStrings("categories-gui.mask.pattern")
        val menu = Menu.builder(pattern.size)
            .setTitle(
                plugin.configYml.getString("categories-gui.title")
                    .replace("%page%", page.toString())
                    .withBattlePassPlaceholders()
            )
        var row = 1
        var num = ((page - 1) * getPerPage())
        pattern.forEach {
            var col = 1
            it.toCharArray().forEach { s ->
                kotlin.run {
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
                val nameKey = getStringOrNull("name")
                val itemStr = getString("item").withBattlePassPlaceholders()
                if (nameKey != null && !itemStr.contains("name:")) {
                    set("item", "$itemStr name:\"${nameKey.withBattlePassPlaceholders()}\"")
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
                buildCloseSlot("categories-gui.buttons.close")
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
        val state = if (nextActive) "active" else "inactive"
        val builder = Slot.builder(
            buildPageItem("categories-gui.buttons.next-page", state)
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
        val state = if (prevActive) "active" else "inactive"
        val builder = Slot.builder(
            buildPageItem("categories-gui.buttons.prev-page", state)
        )

        if (prevActive) {
            builder.onLeftClick { _, _ ->
                when {
                    page > 1 -> CategoriesGUI(player, pass, page - 1, backButton).open()
                    else -> BattlePassGUI.createAndOpen(player, pass)
                }
            }
        }
        return builder.build()
    }

    private fun buildPageItem(basePath: String, state: String): org.bukkit.inventory.ItemStack {
        val itemString = plugin.configYml.getStringOrNull("$basePath.item.$state")
            ?: plugin.configYml.getStringOrNull("$basePath.item")
            ?: plugin.configYml.getString("$basePath.material")

        val itemBuilder = ItemStackBuilder(Items.lookup(itemString.withBattlePassPlaceholders()))

        val name = plugin.configYml.getStringOrNull("$basePath.name.$state")
            ?: plugin.configYml.getStringOrNull("$basePath.name")
        if (name != null) {
            itemBuilder.setDisplayName(name.withBattlePassPlaceholders())
        }

        val lore = plugin.configYml.getStringsOrNull("$basePath.lore.$state")
            ?: plugin.configYml.getStringsOrNull("$basePath.lore")
            ?: emptyList()
        itemBuilder.addLoreLines(lore.withBattlePassPlaceholders())

        return itemBuilder.build()
    }

    private fun buildCloseSlot(basePath: String): Slot {
        val itemString = plugin.configYml.getStringOrNull("$basePath.item")
            ?: plugin.configYml.getString("$basePath.material")

        val itemBuilder = ItemStackBuilder(Items.lookup(itemString.withBattlePassPlaceholders()))

        plugin.configYml.getStringOrNull("$basePath.name")?.let {
            itemBuilder.setDisplayName(it.withBattlePassPlaceholders())
        }

        val lore = plugin.configYml.getStringsOrNull("$basePath.lore")
            ?: emptyList()
        itemBuilder.addLoreLines(lore.withBattlePassPlaceholders())

        return Slot.builder(itemBuilder.build())
            .onLeftClick { event, _ ->
                event.whoClicked.closeInventory()
            }.build()
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
