package com.exanthiax.ecobattlepass.tasks

import com.exanthiax.ecobattlepass.plugin
import wiki.ecohub.config.Config
import wiki.ecohub.config.ViolationContext
import wiki.ecohub.core.registry.Registrable
import wiki.ecohub.game.item.Items
import wiki.ecohub.rules.counter.Counters

class BattleTask(private val _id: String, val config: Config) : Registrable {
    val id: String
        get() = _id

    override fun getID(): String {
        return this._id
    }

    val xpGainMethods = config.getSubsections("xp-gain-methods").mapNotNull {
        Counters.compile(it, ViolationContext(plugin, "EcoBattlepass task $id"))
    }

    val name = config.getString("display.display-name")
    val lore = config.getStrings("display.lore")
    val testable = Items.lookup(config.getString("display.item"))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BattleTask) return false
        return _id == other._id
    }

    override fun hashCode(): Int = _id.hashCode()

    override fun toString(): String = "BattleTask(id='$_id')"
}