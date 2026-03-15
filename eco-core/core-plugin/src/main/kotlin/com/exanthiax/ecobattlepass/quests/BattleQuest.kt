package com.exanthiax.ecobattlepass.quests

import com.exanthiax.ecobattlepass.tasks.ActiveBattleTask
import com.exanthiax.ecobattlepass.tiers.TierType
import com.exanthiax.ecobattlepass.tiers.Tiered
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.registry.Registrable

class BattleQuest(private val _id: String, val config: Config) : Registrable, Tiered {
    override fun getID(): String {
        return _id
    }

    override val tier: TierType = TierType.entries.first {
        it.name.equals(config.getString("battlepass-tier"), true)
    }

    val itemString = config.getString("display.item")

    // val item = Items.lookup(config.getString("display.item"))

    val displayName = config.getString("display.display-name")

    val displayLore = config.getStrings("display.description")

    val tierPoints = config.getInt("battlepass-points")

    val taskAmount = config.getInt("task-amount")

    val tasks = config.getSubsections("tasks").mapNotNull {
        PreparedBattleTask(
            it
        )
    }
}

data class PreparedBattleTask(val config: Config) {
    fun toActiveBattleTask(quest: ActiveBattleQuest): ActiveBattleTask {
        return ActiveBattleTask(config, quest)
    }
}