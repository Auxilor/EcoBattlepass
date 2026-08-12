package com.exanthiax.ecobattlepass.tasks

import com.exanthiax.ecobattlepass.plugin
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.registry.Registrable
import com.willfp.libreforge.ViolationContext
import com.willfp.libreforge.counters.Counters

class BattleTask(private val _id: String, val config: Config) : Registrable {
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