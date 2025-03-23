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
import ru.oftendev.xbattlepass.api.giveTaskExperience
import ru.oftendev.xbattlepass.battlepass.BattlePass
import ru.oftendev.xbattlepass.categories.Categories

object EffectGiveTaskExp: Effect<NoCompileData>("give_battlepass_task_exp") {
    override val arguments: ConfigArguments = arguments {
        require("amount", "You must specify the exp amount!")
        require("task", "You must specify the task to give exp for!")
        require("quest", "You must specify the quest to give exp for!")
    }

    override val parameters: Set<TriggerParameter> = setOf(TriggerParameter.PLAYER)

    override fun onTrigger(config: Config, data: TriggerData, compileData: NoCompileData): Boolean {
        val player = data.player ?: return false
        val amount = config.getDoubleFromExpression("amount", player)
        val task = config.getString("task")
        val quest = config.getString("quest")

        val activeQuest = BattlePass.getActiveQuest(quest) ?: return false

        val activeTask = activeQuest.tasks.firstOrNull { it.parent.id.equals(task, true) } ?: return false

        player.giveTaskExperience(activeTask, amount)

        return true
    }
}