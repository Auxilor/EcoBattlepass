package com.exanthiax.ecobattlepass.libreforge.conditions

import com.exanthiax.ecobattlepass.api.getTier
import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.ConfigArguments
import com.willfp.libreforge.Dispatcher
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.ProvidedHolder
import com.willfp.libreforge.arguments
import com.willfp.libreforge.conditions.Condition
import com.willfp.libreforge.get
import org.bukkit.entity.Player

object ConditionHasBPTier: Condition<NoCompileData>("has_battlepass_tier") {
    override val arguments: ConfigArguments = arguments {
        require("tier", "You must specify the battlepass tier!")
        require("battlepass", "You must specify a battlepass!",
        )
    }

    override fun isMet(
        dispatcher: Dispatcher<*>,
        config: Config,
        holder: ProvidedHolder,
        compileData: NoCompileData
    ): Boolean {
        val player = dispatcher.get<Player>() ?: return false

        val pass = BattlePasses.getByID(config.getString("battlepass")) ?: return false

        return player.getTier(pass) >= config.getIntFromExpression("tier", player)
    }
}