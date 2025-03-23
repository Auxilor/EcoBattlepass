package ru.oftendev.xbattlepass.rewards

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.registry.Registrable
import com.willfp.eco.util.formatEco
import com.willfp.libreforge.ViolationContext
import com.willfp.libreforge.effects.Effects
import com.willfp.libreforge.toDispatcher
import com.willfp.libreforge.triggers.TriggerData
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import ru.oftendev.xbattlepass.api.events.PlayerRewardEvent
import ru.oftendev.xbattlepass.plugin

class Reward(private val _id: String, val config: Config): Registrable {
    override fun getID(): String {
        return this._id
    }

    fun getDisplayName(player: Player): String {
        return this.config.getString("display.name").formatEco(player, true)
    }

    val rewardEffects = Effects.compileChain(
        config.getSubsections("effects"),
        ViolationContext(
            plugin,
            "xBattlepass reward $_id"
        )
    )

    val rewardLoreUnformatted = config.getStrings(
        "display.reward-lore"
    )

    fun grant(player: Player) {
        val event = PlayerRewardEvent(player, this)

        Bukkit.getPluginManager().callEvent(event)

        if (!event.isCancelled) {
            rewardEffects?.trigger(
                player.toDispatcher(),
                data = TriggerData(
                    player = player,
                    dispatcher = player.toDispatcher(),
                    value = 1.0,
                    text = this._id
                )
            ) ?: run {
                plugin.logger.warning("Failed to grant reward $_id to ${player.name}, reward chain is null!")
            }
        }
    }
}