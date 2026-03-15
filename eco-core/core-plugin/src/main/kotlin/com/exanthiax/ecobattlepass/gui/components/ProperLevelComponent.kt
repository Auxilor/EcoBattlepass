package com.exanthiax.ecobattlepass.gui.components

import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.onLeftClick
import com.willfp.eco.core.gui.slot.Slot
import com.willfp.eco.core.map.nestedMap
import com.willfp.ecomponent.AutofillComponent
import com.willfp.ecomponent.GUIPosition
import com.willfp.ecomponent.components.LevelState
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.math.ceil
import kotlin.properties.Delegates

private val progressionOrder = "123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()

@Suppress("MemberVisibilityCanBePrivate")
abstract class ProperLevelComponent : AutofillComponent() {
    private val slots = nestedMap<GUIPosition, Int, Slot?>()

    private val progressionSlots = mutableMapOf<GUIPosition, Int>()

    private var isBuilt = false

    abstract val pattern: List<String>

    abstract val maxLevel: Int

    private fun buildSlot(level: Int) = Slot.builder { player, menu ->
        getLevelItem(
            player,
            menu,
            level,
            getLevelState(
                player,
                level
            )
        )
    }.onLeftClick { player, _, _, _ ->
        getLeftClickAction(player, level, getLevelState(player, level))()
    }
        .setUpdater { player, menu, _ ->
            getLevelItem(
                player,
                menu,
                level,
                getLevelState(
                    player,
                    level
                )
            )
        }
        .build()

    override fun getSlotAt(row: Int, column: Int, player: Player, menu: Menu): Slot? {
        if (!isBuilt) {
            build()
        }

        val position = GUIPosition(row, column)

        return slots[position].getOrPut(menu.getPage(player)) {
            val offset = progressionSlots[position] ?: return null
            val base = (menu.getPage(player) - 1) * levelsPerPage
            val level = offset + base

            if (level > maxLevel) {
                null
            } else {
                buildSlot(level)
            }
        }
    }

    private var _levelsPerPage by Delegates.notNull<Int>()

    val levelsPerPage: Int
        get() {
            if (!isBuilt) {
                build()
            }

            return _levelsPerPage
        }

    private var _pages by Delegates.notNull<Int>()

    val pages: Int
        get() {
            if (!isBuilt) {
                build()
            }

            return _pages
        }

    private fun build() {
        isBuilt = true


        var x = 0
        for (row in pattern) {
            x++
            var y = 0
            for (char in row) {
                y++
                if (char == '0') {
                    continue
                }

                val pos = progressionOrder.indexOf(char)

                if (pos == -1) {
                    continue
                }

                progressionSlots[GUIPosition(x, y)] = pos + 1
            }
        }

        _levelsPerPage = progressionSlots.size
        _pages = ceil(maxLevel.toDouble() / levelsPerPage).toInt()
    }

    fun getPageOf(level: Int): Int {
        if (!isBuilt) {
            build()
        }

        return ceil(level.toDouble() / levelsPerPage).toInt()
    }

    /** Get the item to be shown given a specific [level] and [levelState]. */
    abstract fun getLevelItem(player: Player, menu: Menu, level: Int, levelState: LevelState): ItemStack

    /** Get the state given a [player]'s [level]. */
    abstract fun getLevelState(player: Player, level: Int): LevelState

    /** Get the action given a [player]'s [level]. */
    abstract fun getLeftClickAction(player: Player, level: Int, levelState: LevelState): () -> Unit
}