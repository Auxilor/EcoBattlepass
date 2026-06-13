package com.exanthiax.ecobattlepass.gui

import com.willfp.eco.core.gui.GUIComponent
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.page.Page
import com.willfp.eco.core.gui.page.PageChanger
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.Slot
import com.willfp.eco.core.sound.PlayableSound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

enum class PageButtonState { ACTIVE, INACTIVE }

fun String.withPagePlaceholders(page: Int, maxPage: Int): String =
    this.replace("%page%", page.toString())
        .replace("%max_page%", maxPage.toString())

class PageChangerComponent(
    private val direction: PageChanger.Direction,
    private val sound: PlayableSound?,
    private val itemProvider: (state: PageButtonState, page: Int, maxPage: Int) -> ItemStack?
) : GUIComponent {
    override fun getRows() = 1
    override fun getColumns() = 1

    override fun getSlotAt(row: Int, column: Int, player: Player, menu: Menu): Slot? {
        val page = menu.getPage(player)
        val maxPage = menu.getMaxPage(player)

        val isInactive = (page <= 1 && direction == PageChanger.Direction.BACKWARDS)
                || (page >= maxPage && direction == PageChanger.Direction.FORWARDS)

        if (isInactive) {
            val item = itemProvider(PageButtonState.INACTIVE, page, maxPage) ?: return null
            return slot(item)
        }

        val item = itemProvider(PageButtonState.ACTIVE, page, maxPage) ?: return null
        return slot(item) {
            onLeftClick { event, _, clickedMenu ->
                val clicker = event.whoClicked as Player
                val current = clickedMenu.getPage(clicker)
                val newPage = (current + direction.change)
                    .coerceIn(1, clickedMenu.getMaxPage(clicker))

                if (newPage == current) {
                    return@onLeftClick
                }

                clickedMenu.setState(clicker, Page.PAGE_KEY, newPage)
                sound?.playTo(clicker)
            }
        }
    }
}

fun Menu.refreshPageTitle(player: Player, rawTitle: String, maxPage: Int) {
    val title = rawTitle.withPagePlaceholders(this.getPage(player), maxPage)

    if (this.getState<String>(player, "pagination.shownTitle") == title) {
        return
    }

    if (!player.openInventory.topInventory.type.isCreatable) {
        return
    }

    @Suppress("DEPRECATION")
    player.openInventory.setTitle(title)
    this.setState(player, "pagination.shownTitle", title)
}
