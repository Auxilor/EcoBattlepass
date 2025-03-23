package ru.oftendev.xbattlepass.tasks

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.registry.Registrable
import com.willfp.eco.util.formatEco
import com.willfp.libreforge.ViolationContext
import com.willfp.libreforge.counters.Counters
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.oftendev.xbattlepass.battlepass.TierType
import ru.oftendev.xbattlepass.battlepass.Tiered
import ru.oftendev.xbattlepass.plugin

class BattleTask(private val _id: String, val config: Config): Registrable {
    override fun getID(): String {
        return this._id
    }

    val xpGainMethods = config.getSubsections("xp-gain-methods").mapNotNull {
        Counters.compile(it, ViolationContext(plugin, "xBattlepass task $id"))
    }

    val name = config.getString("display.display-name")
    val lore = config.getStrings("display.lore")
    val testable = Items.lookup(config.getString("display.item"))
}