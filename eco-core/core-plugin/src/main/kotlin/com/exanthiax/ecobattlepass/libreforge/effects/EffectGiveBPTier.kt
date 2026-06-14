package com.exanthiax.ecobattlepass.libreforge.effects

import com.exanthiax.ecobattlepass.api.events.PlayerTierLevelUpEvent
import com.exanthiax.ecobattlepass.api.getTier
import com.exanthiax.ecobattlepass.api.setTier
import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.ArgType
import com.willfp.libreforge.ConfigArguments
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.arguments
import com.willfp.libreforge.effects.Effect
import com.willfp.libreforge.triggers.TriggerData
import com.willfp.libreforge.triggers.TriggerParameter
import org.bukkit.Bukkit

object EffectGiveBPTier: Effect<NoCompileData>("give_battlepass_tiers") {
    override val description = "Gives the player additional battlepass tiers on top of their current tier."

    override val categories = setOf("economy")

    override val additionalInfo = listOf(
        "Can be cancelled by other plugins listening to the tier level up event."
    )

    override val arguments: ConfigArguments = arguments {
        require(
            "tiers",
            "You must specify the amount of tiers to give!",
            description = "The number of battlepass tiers to give. Supports expressions.",
            type = ArgType.EXPRESSION
        )
        require("battlepass",
            "You must specify a battlepass!",
            {passId -> BattlePasses.getByID(passId)},
            {battlepass -> battlepass != null}
        )
        describe("battlepass",
            description = "The ID of the battlepass to give tiers for.",
            type = ArgType.STRING
        )
    }

    override val parameters: Set<TriggerParameter> = setOf(TriggerParameter.PLAYER)

    override fun onTrigger(config: Config, data: TriggerData, compileData: NoCompileData): Boolean {
        val player = data.player ?: return false
        val amount = config.getIntFromExpression("tiers", player)
        val pass = BattlePasses.getByID(config.getString("battlepass")) ?: return false

        val event = PlayerTierLevelUpEvent(player, pass, player.getTier(pass) + amount)

        Bukkit.getPluginManager().callEvent(event)

        if (!event.isCancelled) {
            player.setTier(pass, player.getTier(pass) + amount)
            return true
        }

        return false
    }
}