package com.exanthiax.ecobattlepass.libreforge.effects

import com.exanthiax.ecobattlepass.api.events.PlayerTierLevelUpEvent
import com.exanthiax.ecobattlepass.api.setTier
import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import org.bukkit.Bukkit
import wiki.ecohub.config.ArgType
import wiki.ecohub.config.Config
import wiki.ecohub.config.ConfigArguments
import wiki.ecohub.config.arguments
import wiki.ecohub.core.registry.NoCompileData
import wiki.ecohub.rules.effect.Effect
import wiki.ecohub.rules.trigger.TriggerData
import wiki.ecohub.rules.trigger.TriggerParameter

object EffectSetBPTier: Effect<NoCompileData>("set_battlepass_tier") {
    override val description = "Sets the player's battlepass tier to the given value."

    override val categories = setOf("economy")

    override val arguments: ConfigArguments = arguments {
        require(
            "tier",
            "You must specify the tier to set!",
            description = "The battlepass tier to set the player to.",
            type = ArgType.EXPRESSION,
            example = "5 + %level%"
        )
        require(
            "battlepass",
            "You must specify a battlepass!",
            { passId -> BattlePasses.getByID(passId) },
            { battlepass -> battlepass != null }
        )
        describe(
            "battlepass",
            description = "The ID of the battlepass to set the tier for.",
            type = ArgType.STRING,
            example = "halloween_pass"
        )
    }

    override val parameters: Set<TriggerParameter> = setOf(TriggerParameter.PLAYER)

    override fun onTrigger(config: Config, data: TriggerData, compileData: NoCompileData): Boolean {
        val player = data.player ?: return false
        val amount = config.getIntFromExpression("tier", player)
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