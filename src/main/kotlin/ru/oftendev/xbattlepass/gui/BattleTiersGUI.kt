package ru.oftendev.xbattlepass.gui

import com.github.benmanes.caffeine.cache.Caffeine
import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.menu.MenuLayer
import com.willfp.eco.core.gui.page.PageChanger
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.map.nestedMap
import com.willfp.eco.core.placeholder.context.placeholderContext
import com.willfp.eco.util.NumberUtils.evaluateExpression
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.openMenu
import com.willfp.ecomponent.components.LevelState
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.oftendev.xbattlepass.api.bpTier
import ru.oftendev.xbattlepass.api.hasReceivedTier
import ru.oftendev.xbattlepass.api.receiveTier
import ru.oftendev.xbattlepass.battlepass.BattlePass
import ru.oftendev.xbattlepass.gui.components.ProperLevelComponent
import ru.oftendev.xbattlepass.plugin
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

object BattleTiersGUI {
    fun createAndOpen(player: Player, backButton: Boolean = false) {
        val maskPattern = plugin.configYml.getStrings("tiers-gui.mask.pattern").toTypedArray()
        val maskItems = MaskItems.fromItemNames(plugin.configYml.getStrings("tiers-gui.mask.materials"))

        val levelComponent = BattleTierComponent(plugin)

        val menu = menu(maskPattern.size) {
            title = plugin.configYml.getString("tiers-gui.title").formatEco()

            maxPages(levelComponent.pages)

            setMask(
                FillerMask(
                    maskItems,
                    *maskPattern
                )
            )

            addComponent(1, 1, levelComponent)

            defaultPage {
                levelComponent.getPageOf(it.bpTier).coerceAtLeast(1)
            }

            // Instead of the page changer, this will show up when on the first page
            if (backButton) {
                addComponent(
                    MenuLayer.LOWER,
                    plugin.configYml.getInt("tiers-gui.buttons.prev-page.location.row"),
                    plugin.configYml.getInt("tiers-gui.buttons.prev-page.location.column"),
                    slot(
                        ItemStackBuilder(Items.lookup(plugin.configYml.getString("tiers-gui.buttons.prev-page.material")))
                            .setDisplayName(plugin.configYml.getString("tiers-gui.buttons.prev-page.name"))
                            .build()
                    ) {
                        onLeftClick { _, _ -> BattlePassGUI.createAndOpen(player) }
                    }
                )
            }

            addComponent(
                plugin.configYml.getInt("tiers-gui.buttons.prev-page.location.row"),
                plugin.configYml.getInt("tiers-gui.buttons.prev-page.location.column"),
                PageChanger(
                    ItemStackBuilder(Items.lookup(plugin.configYml.getString("tiers-gui.buttons.prev-page.material")))
                        .setDisplayName(plugin.configYml.getString("tiers-gui.buttons.prev-page.name"))
                        .build(),
                    PageChanger.Direction.BACKWARDS
                )
            )

            addComponent(
                plugin.configYml.getInt("tiers-gui.buttons.next-page.location.row"),
                plugin.configYml.getInt("tiers-gui.buttons.next-page.location.column"),
                PageChanger(
                    ItemStackBuilder(Items.lookup(plugin.configYml.getString("tiers-gui.buttons.next-page.material")))
                        .setDisplayName(plugin.configYml.getString("tiers-gui.buttons.next-page.name"))
                        .build(),
                    PageChanger.Direction.FORWARDS
                )
            )

            setSlot(
                plugin.configYml.getInt("tiers-gui.buttons.close.location.row"),
                plugin.configYml.getInt("tiers-gui.buttons.close.location.column"),
                slot(
                    ItemStackBuilder(Items.lookup(plugin.configYml.getString("tiers-gui.buttons.close.material")))
                        .setDisplayName(plugin.configYml.getString("tiers-gui.buttons.close.name"))
                        .build()
                ) {
                    onLeftClick { event, _ ->
                        event.whoClicked.closeInventory()
                    }
                }
            )

            for (config in plugin.configYml.getSubsections("tiers-gui.buttons.custom-slots")) {
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

private val levelItemCache = Caffeine.newBuilder()
    .expireAfterWrite(plugin.configYml.getInt("gui-cache-ttl").toLong(), TimeUnit.MILLISECONDS)
    .build<Int, ItemStack>()

class BattleTierComponent(
    private val plugin: EcoPlugin
) : ProperLevelComponent() {
    override val pattern: List<String> = plugin.configYml.getStrings("tiers-gui.mask.progression-pattern")
    override val maxLevel = BattlePass.tiers.maxOf { it.number }

    private val itemCache = nestedMap<LevelState, Int, ItemStack>()

    override fun getLevelItem(player: Player, menu: Menu, level: Int, levelState: LevelState): ItemStack {
        val key = run {
            if (levelState == LevelState.UNLOCKED && player.hasReceivedTier(level)) {
                "claimed"
            } else levelState.key
        }

        fun item() = levelItemCache.get(player.hashCode() xor level.hashCode()) {
            val tier = BattlePass.getTier(level)!!
            
            ItemStackBuilder(Items.lookup(plugin.configYml.getString("tiers-gui.buttons.$key.item")))
                .setDisplayName(
                    plugin.configYml.getString("tiers-gui.buttons.$key.name")
                        .let { tier.format(it, player) }
                )
                .addLoreLines(
                    tier.format(
                        plugin.configYml.getStrings("tiers-gui.buttons.$key.lore"),
                        player,
                    )/*.lineWrap(plugin.configYml.getInt("gui.skill-icon.line-wrap"))*/
                )
                .setAmount(
                    evaluateExpression(
                        plugin.configYml.getString("tiers-gui.buttons.item-amount")
                            .replace("%level%", level.toString()),
                        placeholderContext(
                            player = player
                        )
                    ).roundToInt()
                )
                .build()
        }

        return if (levelState != LevelState.IN_PROGRESS) {
            itemCache[levelState].getOrPut(level) { item() }
        } else {
            item()
        }
    }

    override fun getLevelState(player: Player, level: Int): LevelState {
        return when {
            level <= player.bpTier -> LevelState.UNLOCKED
            level == player.bpTier + 1 -> LevelState.IN_PROGRESS
            else -> LevelState.LOCKED
        }
    }

    override fun getLeftClickAction(player: Player, level: Int, levelState: LevelState): () -> Unit {
        val key = run {
            if (levelState == LevelState.UNLOCKED && player.hasReceivedTier(level)) {
                "claimed"
            } else levelState.key
        }

        return if (key == "unlocked") {
            {
                val tier = BattlePass.getTier(level)
                if (tier != null) {
                    levelItemCache.invalidate(level)
                    itemCache[levelState]?.remove(level)
                    player.receiveTier(tier)
                    player.openMenu?.refresh(player)
                    // player.closeInventory()
                }
            }
        } else {
            {}
        }
    }
}