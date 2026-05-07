package com.exanthiax.ecobattlepass.commands.helpers

import com.exanthiax.ecobattlepass.api.getTier
import com.exanthiax.ecobattlepass.api.hasPremium
import com.exanthiax.ecobattlepass.api.hasReceivedTier
import com.exanthiax.ecobattlepass.api.receiveTier
import com.exanthiax.ecobattlepass.api.receiveTierFreeOnly
import com.exanthiax.ecobattlepass.api.receiveTierPremiumOnly
import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.gui.components.invalidateTierItemCache
import com.exanthiax.ecobattlepass.utils.ReceivedTierState
import com.willfp.eco.util.openMenu
import org.bukkit.entity.Player

object ClaimHandler {

    /**
     * Routes: /<pass> claim <tier|all> [free|premium]
     */
    fun handleClaim(player: Player, pass: BattlePass, args: MutableList<String>) {
        val tierArg = args.getOrNull(1)?.lowercase() ?: run {
            Messages.sendDynamicPassUsage(player)
            return
        }

        val typeArg = args.getOrNull(2)?.lowercase()

        if (tierArg == "all") {
            handleClaimAll(player, pass, typeArg)
        } else {
            val tierNumber = tierArg.toIntOrNull() ?: run {
                Messages.sendDynamicPassUsage(player)
                return
            }
            handleClaimSingle(player, pass, tierNumber, typeArg)
        }
    }

    /**
     * Claims rewards for a single tier.
     */
    fun handleClaimSingle(player: Player, pass: BattlePass, tierNumber: Int, typeArg: String?) {
        val tier = pass.getTier(tierNumber) ?: run {
            Messages.sendTierNotFound(player, tierNumber)
            return
        }

        if (tier.number != tierNumber) {
            Messages.sendTierNotFound(player, tierNumber)
            return
        }

        val playerTier = player.getTier(pass)
        if (tierNumber > playerTier) {
            Messages.sendTierNotUnlocked(player, tierNumber)
            return
        }

        val receivedState = player.hasReceivedTier(pass, tierNumber)

        when (typeArg) {
            null -> {
                when (receivedState) {
                    ReceivedTierState.RECEIVED -> {
                        Messages.sendTierAlreadyClaimed(player, tierNumber)
                        return
                    }

                    ReceivedTierState.RECEIVED_FREE -> {
                        if (player.hasPremium(pass)) {
                            player.receiveTierPremiumOnly(tier)
                        } else {
                            Messages.sendTierAlreadyClaimed(player, tierNumber)
                            return
                        }
                    }

                    ReceivedTierState.RECEIVED_PREMIUM -> {
                        player.receiveTierFreeOnly(tier)
                    }

                    ReceivedTierState.NOT_RECEIVED -> {
                        player.receiveTier(tier)
                    }
                }
            }

            "free" -> {
                when (receivedState) {
                    ReceivedTierState.RECEIVED,
                    ReceivedTierState.RECEIVED_FREE -> {
                        Messages.sendTierAlreadyClaimed(player, tierNumber)
                        return
                    }

                    ReceivedTierState.RECEIVED_PREMIUM,
                    ReceivedTierState.NOT_RECEIVED -> {
                        player.receiveTierFreeOnly(tier)
                    }
                }
            }

            "premium" -> {
                if (!player.hasPremium(pass)) {
                    Messages.sendClaimNoPremium(player)
                    return
                }
                when (receivedState) {
                    ReceivedTierState.RECEIVED,
                    ReceivedTierState.RECEIVED_PREMIUM -> {
                        Messages.sendTierAlreadyClaimed(player, tierNumber)
                        return
                    }

                    ReceivedTierState.RECEIVED_FREE,
                    ReceivedTierState.NOT_RECEIVED -> {
                        player.receiveTierPremiumOnly(tier)
                    }
                }
            }

            else -> {
                Messages.sendDynamicPassUsage(player)
                return
            }
        }

        invalidateTierItemCache()
        player.openMenu?.refresh(player)
    }

    /**
     * Claims all available tier rewards.
     */
    fun handleClaimAll(player: Player, pass: BattlePass, typeArg: String?) {
        if (typeArg == "premium" && !player.hasPremium(pass)) {
            Messages.sendClaimNoPremium(player)
            return
        }

        val playerTier = player.getTier(pass)
        var claimedCount = 0

        for (tier in pass.tiers) {
            if (tier.number > playerTier) continue

            val receivedState = player.hasReceivedTier(pass, tier.number)

            when (typeArg) {
                null -> {
                    when (receivedState) {
                        ReceivedTierState.RECEIVED -> continue
                        ReceivedTierState.RECEIVED_FREE -> {
                            if (player.hasPremium(pass)) {
                                player.receiveTierPremiumOnly(tier)
                                claimedCount++
                            }
                        }

                        ReceivedTierState.RECEIVED_PREMIUM -> {
                            player.receiveTierFreeOnly(tier)
                            claimedCount++
                        }

                        ReceivedTierState.NOT_RECEIVED -> {
                            player.receiveTier(tier)
                            claimedCount++
                        }
                    }
                }

                "free" -> {
                    when (receivedState) {
                        ReceivedTierState.RECEIVED,
                        ReceivedTierState.RECEIVED_FREE -> continue

                        ReceivedTierState.RECEIVED_PREMIUM,
                        ReceivedTierState.NOT_RECEIVED -> {
                            player.receiveTierFreeOnly(tier)
                            claimedCount++
                        }
                    }
                }

                "premium" -> {
                    when (receivedState) {
                        ReceivedTierState.RECEIVED,
                        ReceivedTierState.RECEIVED_PREMIUM -> continue

                        ReceivedTierState.RECEIVED_FREE,
                        ReceivedTierState.NOT_RECEIVED -> {
                            player.receiveTierPremiumOnly(tier)
                            claimedCount++
                        }
                    }
                }

                else -> {
                    Messages.sendDynamicPassUsage(player)
                    return
                }
            }
        }

        if (claimedCount == 0) {
            Messages.sendNoRewardsToClaim(player)
            return
        }

        invalidateTierItemCache()
        player.openMenu?.refresh(player)
        Messages.sendClaimAllSuccess(player, claimedCount)
    }
}
