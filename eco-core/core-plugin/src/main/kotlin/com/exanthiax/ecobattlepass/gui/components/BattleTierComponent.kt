package com.exanthiax.ecobattlepass.gui.components

import com.exanthiax.ecobattlepass.api.getTier
import com.exanthiax.ecobattlepass.api.hasPremium
import com.exanthiax.ecobattlepass.api.hasReceivedTier
import com.exanthiax.ecobattlepass.api.receiveTier
import com.exanthiax.ecobattlepass.api.receiveTierFreeOnly
import com.exanthiax.ecobattlepass.api.receiveTierPremiumOnly
import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.tiers.BPTier
import com.exanthiax.ecobattlepass.tiers.TierType
import com.exanthiax.ecobattlepass.utils.InternalPlaceholders
import com.exanthiax.ecobattlepass.utils.ReceivedTierState
import com.github.benmanes.caffeine.cache.Caffeine
import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.Prerequisite
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.map.nestedMap
import com.willfp.eco.core.placeholder.context.placeholderContext
import com.willfp.eco.core.sound.PlayableSound
import com.willfp.eco.util.NumberUtils.evaluateExpression
import com.willfp.eco.util.openMenu
import com.willfp.ecomponent.components.LevelState
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


private val levelItemCache = Caffeine.newBuilder()
    .expireAfterWrite(
        com.exanthiax.ecobattlepass.plugin.configYml.getInt("gui-cache-ttl").toLong(),
        TimeUnit.MILLISECONDS
    )
    .build<Triple<UUID, Int, TierType?>, ItemStack>()

fun invalidateTierItemCache() {
    levelItemCache.invalidateAll()
}

class BattleTierComponent(
    private val plugin: EcoPlugin,
    private val pass: BattlePass,
    private val tierType: TierType? = null,
    patternPath: String = "tiers-gui.layouts.combined.pattern",
    legacyPatternPath: String? = "tiers-gui.mask.progression-pattern",
    private val emptyTierDisplayMode: EmptyDisplayMode = EmptyDisplayMode.NORMAL,
    private val maxItemAmount: Int =
        (plugin.configYml.getIntOrNull("tiers-gui.buttons.max-item-amount") ?: 64).coerceIn(1, 99)
) : ProperLevelComponent() {
    override val pattern: List<String> = plugin.configYml.getStrings(patternPath)
        .ifEmpty { legacyPatternPath?.let { plugin.configYml.getStrings(it) } ?: emptyList() }
    override val maxLevel = pass.maxLevel
    private val itemCache = nestedMap<LevelState, Int, ItemStack>()

    private fun hasRelevantRewards(tier: BPTier): Boolean {
        return when (tierType) {
            TierType.FREE -> tier.rewards.any { it.tier == TierType.FREE }
            TierType.PREMIUM -> tier.rewards.any { it.tier == TierType.PREMIUM }
            null -> tier.rewards.isNotEmpty()
        }
    }

    private fun resolveKey(player: Player, level: Int, levelState: LevelState): String {
        val tier = pass.getTier(level)

        // 1. Null tier — always handle here (there is no BPTier to check hasReceivedTier)
        tier ?: return when (emptyTierDisplayMode) {
            EmptyDisplayMode.HIDDEN -> "hidden"
            EmptyDisplayMode.HIDDEN_BEHIND_LEVEL -> if (levelState == LevelState.UNLOCKED) "hidden" else levelState.key
            EmptyDisplayMode.ALL_CLAIMED -> "claimed"
            EmptyDisplayMode.BEHIND_LEVEL -> if (levelState == LevelState.UNLOCKED) "claimed" else levelState.key
            EmptyDisplayMode.NORMAL -> levelState.key
        }

        // 2. Tier with no relevant rewards + NOT normal mode → apply special display mode
        //    In NORMAL mode, empty tiers fall back to the normal logic (step 3+)
        //    so that hasReceivedTier works and they are marked as claimed upon clicking
        if (!hasRelevantRewards(tier) && emptyTierDisplayMode != EmptyDisplayMode.NORMAL) {
            return when (emptyTierDisplayMode) {
                EmptyDisplayMode.HIDDEN -> "hidden"
                EmptyDisplayMode.HIDDEN_BEHIND_LEVEL -> if (levelState == LevelState.UNLOCKED) "hidden" else levelState.key
                EmptyDisplayMode.ALL_CLAIMED -> "claimed"
                EmptyDisplayMode.BEHIND_LEVEL -> if (levelState == LevelState.UNLOCKED) "claimed" else levelState.key
            }
        }

        // 3. If the tier is locked, we don’t waste time calculating rewards
        if (levelState != LevelState.UNLOCKED) {
            return levelState.key
        }

        // 4. Rewards Logic
        val receivedState = player.hasReceivedTier(pass, level)

        return when (tierType) {
            TierType.FREE -> {
                if (receivedState == ReceivedTierState.RECEIVED ||
                    receivedState == ReceivedTierState.RECEIVED_FREE
                ) "claimed"
                else "unlocked"
            }

            TierType.PREMIUM -> {
                val isReceived = receivedState == ReceivedTierState.RECEIVED ||
                        receivedState == ReceivedTierState.RECEIVED_PREMIUM

                when {
                    isReceived -> "claimed"
                    player.hasPremium(pass) -> "unlocked"
                    else -> "premium-required"
                }
            }

            // Case for mixed or generic tiers (combined mode)
            null -> when (receivedState) {
                ReceivedTierState.RECEIVED -> "claimed"
                ReceivedTierState.RECEIVED_FREE -> {
                    if (player.hasPremium(pass)) "unlocked-free" else "premium-required"
                }

                ReceivedTierState.RECEIVED_PREMIUM -> "unlocked"
                else -> levelState.key
            }
        }
    }

    override fun getLevelItem(player: Player, menu: Menu, level: Int, levelState: LevelState): ItemStack {
        val key = resolveKey(player, level, levelState)
        if (key == "hidden") return ItemStack.empty()

        fun item() = levelItemCache.get(Triple(player.uniqueId, level, tierType)) {
            val tier = pass.getTier(level) ?: BPTier(level, pass)

            // Determine the track-specific path for per-level overrides
            val trackPath = when (tierType) {
                TierType.FREE -> "free-track"
                TierType.PREMIUM -> "premium-track"
                null -> null
            }

            // Determine the base config path based on tierType (split mode) or combined mode
            val basePath = when (tierType) {
                TierType.FREE -> "tiers-gui.buttons.free-track.$key"
                TierType.PREMIUM -> "tiers-gui.buttons.premium-track.$key"
                null -> "tiers-gui.buttons.$key"
            }

            // Try per-level override first (tier-specific config)
            // Priority: track-specific override > generic override > config.yml
            val displayItem = (trackPath?.let { tier.config.getStringOrNull("display.$it.$key.item") })
                ?: tier.config.getStringOrNull("display.$key.item")
                ?: plugin.configYml.getStringOrNull("$basePath.item")
                ?: plugin.configYml.getString("tiers-gui.buttons.$key.item")

            val displayName = (trackPath?.let { tier.config.getStringOrNull("display.$it.$key.name") })
                ?: tier.config.getStringOrNull("display.$key.name")
                ?: plugin.configYml.getStringOrNull("$basePath.name")
                ?: plugin.configYml.getString("tiers-gui.buttons.$key.name")

            val displayLore = if (trackPath != null && tier.config.has("display.$trackPath.$key.lore"))
                tier.config.getStrings("display.$trackPath.$key.lore")
            else if (tier.config.has("display.$key.lore"))
                tier.config.getStrings("display.$key.lore")
            else if (plugin.configYml.has("$basePath.lore"))
                plugin.configYml.getStrings("$basePath.lore")
            else
                plugin.configYml.getStrings("tiers-gui.buttons.$key.lore")

            val resolvedItem = InternalPlaceholders.TierPlaceholders.replace(displayItem, tier, pass, player)
            val amount = evaluateExpression(
                plugin.configYml.getString("tiers-gui.buttons.item-amount")
                    .replace("%level%", level.toString()),
                placeholderContext(player = player)
            ).roundToInt().coerceIn(1, maxItemAmount)

            val builtItem = ItemStackBuilder(Items.lookup(resolvedItem))
                .setDisplayName(tier.format(displayName, player, tierType).firstOrNull() ?: "")
                .addLoreLines(tier.format(displayLore, player, tierType))
                .setAmount(amount)
                .build()
            if (Prerequisite.HAS_PAPER.isMet && maxItemAmount > 64) {
                val meta = builtItem.itemMeta
                meta.setMaxStackSize(maxItemAmount)
                builtItem.itemMeta = meta
            }
            builtItem
        }

        return if (levelState != LevelState.IN_PROGRESS) {
            itemCache[levelState].getOrPut(level) { item() }
        } else {
            item()
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
            .firstOrNull { it.tier.name.equals("premium", true) }
            ?.reward?.getDisplayName(this) ?: return
        sendMessage(
            InternalPlaceholders.TierPlaceholders
                .replace(plugin.langYml.getMessage("premium-required"), tier, pass, this)
                .replace("%tier%", tierLevel.toString())
                .replace("%reward%", premiumRewardName)
        )
        PlayableSound.create(plugin.configYml.getSubsection("sound.premium-required"))?.playTo(this)
    }

    override fun getLeftClickAction(player: Player, level: Int, levelState: LevelState): () -> Unit {
        return when (val key = resolveKey(player, level, levelState)) {
            "unlocked", "unlocked-free", "premium-required" -> {
                {
                    val tier = pass.getTier(level)
                    if (tier != null) {
                        if (key == "premium-required") {
                            player.sendPremiumRequiredMessage(level, tier)
                        } else {
                            levelItemCache.invalidate(Triple(player.uniqueId, level, null))
                            levelItemCache.invalidate(Triple(player.uniqueId, level, TierType.FREE))
                            levelItemCache.invalidate(Triple(player.uniqueId, level, TierType.PREMIUM))
                            itemCache[levelState].remove(level)
                            when (tierType) {
                                TierType.FREE -> player.receiveTierFreeOnly(tier)
                                TierType.PREMIUM -> player.receiveTierPremiumOnly(tier)
                                null -> {
                                    if (key == "unlocked") player.receiveTier(tier)
                                    else player.receiveTierPremiumOnly(tier)
                                }
                            }
                            player.openMenu?.refresh(player)
                        }
                    }
                }
            }

            "locked", "in-progress" -> {
                { PlayableSound.create(plugin.configYml.getSubsection("sound.reward-locked"))?.playTo(player) }
            }

            else -> {
                {}
            }
        }
    }
}
