package com.exanthiax.xbattlepass.libreforge.conditions

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.*
import com.willfp.libreforge.conditions.Condition
import org.bukkit.entity.Player
import com.exanthiax.xbattlepass.api.hasPremium
import com.exanthiax.xbattlepass.battlepass.BattlePasses

object ConditionHasBPPremium: Condition<NoCompileData>("has_premium_battlepass") {
    override val arguments: ConfigArguments = arguments {
        require("battlepass", "You must specify a battlepass!")
    }

    override fun isMet(
        dispatcher: Dispatcher<*>,
        config: Config,
        holder: ProvidedHolder,
        compileData: NoCompileData
    ): Boolean {
        val player = dispatcher.get<Player>() ?: return false

        val pass = BattlePasses.getByID(config.getString("battlepass")) ?: return false

        return player.hasPremium(pass)
    }
}