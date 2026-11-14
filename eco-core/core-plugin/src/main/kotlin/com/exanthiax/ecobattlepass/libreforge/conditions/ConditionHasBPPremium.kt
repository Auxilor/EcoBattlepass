package com.exanthiax.ecobattlepass.libreforge.conditions

import com.exanthiax.ecobattlepass.api.hasPremium
import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import org.bukkit.entity.Player
import wiki.ecohub.config.ArgType
import wiki.ecohub.config.Config
import wiki.ecohub.config.ConfigArguments
import wiki.ecohub.config.arguments
import wiki.ecohub.core.registry.NoCompileData
import wiki.ecohub.rules.condition.Condition
import wiki.ecohub.rules.holder.Dispatcher
import wiki.ecohub.rules.holder.ProvidedHolder
import wiki.ecohub.rules.holder.get

object ConditionHasBPPremium: Condition<NoCompileData>("has_premium_battlepass") {
    override val description = "Passes when the player has premium access to the specified battlepass."

    override val categories = setOf("permission")

    override val arguments: ConfigArguments = arguments {
        require(
            "battlepass",
            "You must specify a battlepass!",
            description = "The ID of the battlepass to check premium access for.",
            type = ArgType.STRING,
            example = "halloween_pass"
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