package com.exanthiax.ecobattlepass.libreforge.conditions

import com.exanthiax.ecobattlepass.api.hasPremium
import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.ArgType
import com.willfp.libreforge.ConfigArguments
import com.willfp.libreforge.Dispatcher
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.ProvidedHolder
import com.willfp.libreforge.arguments
import com.willfp.libreforge.conditions.Condition
import com.willfp.libreforge.get
import org.bukkit.entity.Player

object ConditionHasBPPremium: Condition<NoCompileData>("has_premium_battlepass") {
    override val description = "Passes when the player has premium access to the specified battlepass."

    override val categories = setOf("permission")

    override val arguments: ConfigArguments = arguments {
        require(
            "battlepass",
            "You must specify a battlepass!",
            description = "The ID of the battlepass to check premium access for.",
            type = ArgType.STRING
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

        return player.hasPremium(pass)
    }
}