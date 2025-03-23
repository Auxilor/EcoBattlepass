package ru.oftendev.xbattlepass.libreforge.effects

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.ConfigArguments
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.arguments
import com.willfp.libreforge.effects.Effect
import com.willfp.libreforge.triggers.TriggerData
import com.willfp.libreforge.triggers.TriggerParameter
import ru.oftendev.xbattlepass.api.giveBPExperience
import ru.oftendev.xbattlepass.api.giveExactBPExperience

object EffectGiveBPExp: Effect<NoCompileData>("give_battlepass_exp") {
    override val arguments: ConfigArguments = arguments {
        require("amount", "You must specify the exp amount!")
    }

    override val parameters: Set<TriggerParameter> = setOf(TriggerParameter.PLAYER)

    override fun onTrigger(config: Config, data: TriggerData, compileData: NoCompileData): Boolean {
        val player = data.player ?: return false
        val amount = config.getDoubleFromExpression("amount", player)
        val exact = config.getBool("exact")

        if (exact) {
            player.giveExactBPExperience(amount)
        } else {
            player.giveBPExperience(amount)
        }

        return true
    }
}