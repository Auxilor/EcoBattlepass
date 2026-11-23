package com.exanthiax.ecobattlepass.libreforge.conditions

import com.exanthiax.ecobattlepass.api.getTier
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

object ConditionHasBPTier: Condition<NoCompileData>("has_battlepass_tier") {
    override val description = "Passes when the player's battlepass tier is at or above the given tier."

    override val categories = setOf("economy")

    override val arguments: ConfigArguments = arguments {
        require(
            "tier",
            "You must specify the battlepass tier!",
            description = "The minimum battlepass tier required. Supports expressions.",
            type = ArgType.EXPRESSION,
            example = "5 + %level%"
        )
        require(
            "battlepass",
            "You must specify a battlepass!",
            description = "The ID of the battlepass to check the player's tier for.",
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

        return player.getTier(pass) >= config.getIntFromExpression("tier", player)
    }
}