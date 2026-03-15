package com.exanthiax.ecobattlepass.gui.components

import com.exanthiax.ecobattlepass.api.getTier
import com.exanthiax.ecobattlepass.api.hasPremium
import com.exanthiax.ecobattlepass.api.hasReceivedTier
import com.exanthiax.ecobattlepass.api.receiveTier
import com.exanthiax.ecobattlepass.api.receiveTierPremiumOnly
import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.tiers.BPTier
import com.exanthiax.ecobattlepass.utils.ReceivedTierState
import com.github.benmanes.caffeine.cache.Caffeine
import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.map.nestedMap
import com.willfp.eco.core.placeholder.context.placeholderContext
import com.willfp.eco.core.sound.PlayableSound
import com.willfp.eco.util.NumberUtils.evaluateExpression
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.openMenu
import com.willfp.ecomponent.components.LevelState
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

private val levelItemCache = Caffeine.newBuilder()
    .expireAfterWrite(
        com.exanthiax.ecobattlepass.plugin.configYml.getInt("gui-cache-ttl").toLong(),
        TimeUnit.MILLISECONDS
    )
    .build<Int, ItemStack>()

class BattleTierComponent(
    private val plugin: EcoPlugin,
    private val pass: BattlePass
) : ProperLevelComponent() {
    override val pattern: List<String> = plugin.configYml.getStrings("tiers-gui.mask.progression-pattern")
    override val maxLevel = pass.maxLevel

    private val itemCache = nestedMap<LevelState, Int, ItemStack>()

    override fun getLevelItem(player: Player, menu: Menu, level: Int, levelState: LevelState): ItemStack {
        val key: String = run {
            if (levelState == LevelState.UNLOCKED) {
                when (player.hasReceivedTier(pass, level)) {
                    ReceivedTierState.RECEIVED -> "claimed"
                    ReceivedTierState.RECEIVED_FREE -> if (player.hasPremium(pass)) "unlocked-free" else "premium-required"
                    else -> levelState.key
                }
            } else {
                levelState.key
            }
        }

        // plugin.logger.info("Level $level, $key item ${Items.lookup(plugin.configYml.getString("tiers-gui.buttons.$key.item"))}")

        fun item() = levelItemCache.get(player.hashCode() xor level.hashCode()) {
            val tier = pass.getTier(level)!!

            ItemStackBuilder(Items.lookup(plugin.configYml.getString("tiers-gui.buttons.$key.item")))
                .setDisplayName(
                    tier.format(plugin.configYml.getString("tiers-gui.buttons.$key.name"), player).firstOrNull() ?: ""
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
        }.apply {
            // "Slot $level item $this"
        }
    }

    override fun getLevelState(player: Player, level: Int): LevelState {
        return when {
            level <= player.getTier(pass) -> LevelState.UNLOCKED
            level == player.getTier(pass) + 1 -> LevelState.IN_PROGRESS
            else -> LevelState.LOCKED
        }
    }

    private fun Player.sendPremiumRequiredMessage(tierLevel: Int, tier: BPTier) {
        val premiumRewardName = tier.rewards
            .first { it.tier.name.equals("premium", true) }
            .reward.getDisplayName(this)

        sendMessage(
            plugin.langYml.getMessage("premium-required")
                .replace("%tier%", tierLevel.toString())
                .replace("%reward%", premiumRewardName)
        )
        PlayableSound.create(plugin.configYml.getSubsection("sound.premium-required"))?.playTo(this)
    }

    override fun getLeftClickAction(player: Player, level: Int, levelState: LevelState): () -> Unit {
        val key: String = run {
            if (levelState == LevelState.UNLOCKED) {
                when (player.hasReceivedTier(pass, level)) {
                    ReceivedTierState.RECEIVED -> "claimed"
                    ReceivedTierState.RECEIVED_FREE -> if (player.hasPremium(pass)) "unlocked-free" else "premium-required"
                    else -> levelState.key
                }
            } else {
                levelState.key
            }
        }

        return when (key) {
            "unlocked", "unlocked-free", "premium-required" -> {
                {
                    val tier = pass.getTier(level)
                    if (tier != null) {
                        if (key == "premium-required") {
                            val name = tier.rewards.first { it.tier.name.equals("premium", true) }
                                .reward.getDisplayName(player)
                                .formatEco(player, true)

                            player.sendMessage(
                                plugin.langYml.getMessage("premium-required")
                                    .replace("%tier%", level.toString())
                                    .replace("%reward%", name)
                                    .replace("%pass%", pass.name)
                            )
                            PlayableSound.create(plugin.configYml.getSubsection("sound.premium-required"))
                                ?.playTo(player)
                        } else {
                            levelItemCache.invalidate(level)
                            itemCache[levelState].remove(level)

                            if (key == "unlocked") player.receiveTier(tier) else player.receiveTierPremiumOnly(tier)

                            player.openMenu?.refresh(player)
                        }
                    }
                }
            }

            "locked", "in-progress" -> {
                {
                    PlayableSound.create(plugin.configYml.getSubsection("sound.reward-locked"))?.playTo(player)
                }
            }

            else -> {
                {}
            }
        }
    }
}