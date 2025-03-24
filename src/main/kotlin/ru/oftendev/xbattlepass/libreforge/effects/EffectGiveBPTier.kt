package ru.oftendev.xbattlepass.libreforge.effects

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.ConfigArguments
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.arguments
import com.willfp.libreforge.effects.Effect
import com.willfp.libreforge.triggers.TriggerData
import com.willfp.libreforge.triggers.TriggerParameter
import org.bukkit.Bukkit
import ru.oftendev.xbattlepass.api.bpTier
import ru.oftendev.xbattlepass.api.events.PlayerTierLevelUpEvent
import ru.oftendev.xbattlepass.api.giveBPExperience
import ru.oftendev.xbattlepass.api.giveExactBPExperience

object EffectGiveBPTier: Effect<NoCompileData>("give_battlepass_tiers") {
    override val arguments: ConfigArguments = arguments {
        require("tiers", "You must specify the amount of tiers to give!")
    }

    override val parameters: Set<TriggerParameter> = setOf(TriggerParameter.PLAYER)

    override fun onTrigger(config: Config, data: TriggerData, compileData: NoCompileData): Boolean {
        val player = data.player ?: return false
        val amount = config.getIntFromExpression("amount", player)

        val event = PlayerTierLevelUpEvent(player, player.bpTier + amount)

        Bukkit.getPluginManager().callEvent(event)

        if (!event.isCancelled) {
            player.bpTier += amount
            return true
        }

        return false
    }
}