package com.exanthiax.ecobattlepass.libreforge.effects

import com.exanthiax.ecobattlepass.api.events.PlayerTierLevelUpEvent
import com.exanthiax.ecobattlepass.api.getTier
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

object EffectGiveBPTier: Effect<NoCompileData>("give_battlepass_tiers") {
    override val description = "Gives the player additional battlepass tiers on top of their current tier."

    override val categories = setOf("economy")

    override val arguments: ConfigArguments = arguments {
        require(
            "tiers",
            "You must specify the amount of tiers to give!",
            description = "The number of battlepass tiers to give. Supports expressions.",
            type = ArgType.EXPRESSION,
            example = "1 + %level%"
        )
        require(
            "battlepass",
            "You must specify a battlepass!",
            { passId -> BattlePasses.getByID(passId) },
            { battlepass -> battlepass != null }
        )
        describe(
            "battlepass",
            description = "The ID of the battlepass to give tiers for.",
            type = ArgType.STRING,
            example = "halloween_pass"
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