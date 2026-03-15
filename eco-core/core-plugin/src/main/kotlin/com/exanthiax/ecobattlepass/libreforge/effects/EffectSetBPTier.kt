package com.exanthiax.ecobattlepass.libreforge.effects

import com.exanthiax.ecobattlepass.api.events.PlayerTierLevelUpEvent
import com.exanthiax.ecobattlepass.api.setTier
import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.ConfigArguments
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.arguments
import com.willfp.libreforge.effects.Effect
import com.willfp.libreforge.triggers.TriggerData
import com.willfp.libreforge.triggers.TriggerParameter
import org.bukkit.Bukkit

object EffectSetBPTier: Effect<NoCompileData>("set_battlepass_tier") {
    override val arguments: ConfigArguments = arguments {
        require("tier", "You must specify the tier to set!")
        require("battlepass",
            "You must specify a battlepass!",
            {passId -> BattlePasses.getByID(passId)},
            {battlepass -> battlepass != null}
        )
    }

    override val parameters: Set<TriggerParameter> = setOf(TriggerParameter.PLAYER)

    override fun onTrigger(config: Config, data: TriggerData, compileData: NoCompileData): Boolean {
        val player = data.player ?: return false
        val amount = config.getIntFromExpression("amount", player)
        val pass = BattlePasses.getByID(config.getString("battlepass")) ?: return false

        val event = PlayerTierLevelUpEvent(player, pass, amount)

        Bukkit.getPluginManager().callEvent(event)

        if (!event.isCancelled) {
            player.setTier(pass, amount)
            return true
        }

        return false
    }
}