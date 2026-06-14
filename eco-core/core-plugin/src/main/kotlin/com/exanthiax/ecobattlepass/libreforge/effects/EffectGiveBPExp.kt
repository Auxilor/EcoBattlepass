package com.exanthiax.ecobattlepass.libreforge.effects

import com.exanthiax.ecobattlepass.api.giveBPExperience
import com.exanthiax.ecobattlepass.api.giveExactBPExperience
import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.ArgType
import com.willfp.libreforge.ConfigArguments
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.arguments
import com.willfp.libreforge.effects.Effect
import com.willfp.libreforge.triggers.TriggerData
import com.willfp.libreforge.triggers.TriggerParameter

object EffectGiveBPExp: Effect<NoCompileData>("give_battlepass_xp") {
    override val description = "Gives the player experience towards the specified battlepass."

    override val categories = setOf("economy")

    override val arguments: ConfigArguments = arguments {
        require(
            "amount",
            "You must specify the exp amount!",
            description = "The amount of battlepass experience to give. Supports expressions.",
            type = ArgType.EXPRESSION
        )
        require("battlepass",
            "You must specify a battlepass!",
            {passId -> BattlePasses.getByID(passId)},
            {battlepass -> battlepass != null}
        )
        describe("battlepass",
            description = "The ID of the battlepass to give experience for.",
            type = ArgType.STRING
        )
        optional(
            "exact",
            description = "If true, the exact amount is given without applying the player's experience multipliers.",
            type = ArgType.BOOLEAN,
            default = "false"
        )
    }

    override val parameters: Set<TriggerParameter> = setOf(TriggerParameter.PLAYER)

    override fun onTrigger(config: Config, data: TriggerData, compileData: NoCompileData): Boolean {
        val player = data.player ?: return false
        val amount = config.getDoubleFromExpression("amount", player)
        val exact = config.getBool("exact")
        val pass = BattlePasses.getByID(config.getString("battlepass")) ?: return false

        if (exact) {
            player.giveExactBPExperience(pass, amount)
        } else {
            player.giveBPExperience(pass, amount)
        }

        return true
    }
}