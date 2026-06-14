package com.exanthiax.ecobattlepass.libreforge.effects

import com.exanthiax.ecobattlepass.api.giveTaskExperience
import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.ArgType
import com.willfp.libreforge.ConfigArguments
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.arguments
import com.willfp.libreforge.effects.Effect
import com.willfp.libreforge.triggers.TriggerData
import com.willfp.libreforge.triggers.TriggerParameter

object EffectGiveTaskExp: Effect<NoCompileData>("give_battlepass_task_exp") {
    override val description = "Gives the player progress towards a specific battlepass task."

    override val categories = setOf("economy")

    override val arguments: ConfigArguments = arguments {
        require(
            "amount",
            "You must specify the exp amount!",
            description = "The amount of task progress to give.,
            type = ArgType.EXPRESSION
        )
        require(
            "task",
            "You must specify the task to give exp for!",
            description = "The ID of the task to give progress towards.",
            type = ArgType.STRING
        )
        require(
            "quest",
            "You must specify the quest to give exp for!",
            description = "The ID of the quest that the task belongs to.",
            type = ArgType.STRING
        )
        require("battlepass",
            "You must specify a battlepass!",
            {passId -> BattlePasses.getByID(passId)},
            {battlepass -> battlepass != null}
        )
        describe("battlepass",
            description = "The ID of the battlepass that the quest belongs to.",
            type = ArgType.STRING
        )
    }

    override val parameters: Set<TriggerParameter> = setOf(TriggerParameter.PLAYER)

    override fun onTrigger(config: Config, data: TriggerData, compileData: NoCompileData): Boolean {
        val player = data.player ?: return false
        val amount = config.getDoubleFromExpression("amount", player)
        val task = config.getString("task")
        val quest = config.getString("quest")
        val pass = BattlePasses.getByID(config.getString("battlepass")) ?: return false

        val activeQuest = pass.getActiveQuest(quest) ?: return false

        val activeTask = activeQuest.tasks.firstOrNull { it.parent.id.equals(task, true) } ?: return false

        player.giveTaskExperience(activeTask, amount)

        return true
    }
}