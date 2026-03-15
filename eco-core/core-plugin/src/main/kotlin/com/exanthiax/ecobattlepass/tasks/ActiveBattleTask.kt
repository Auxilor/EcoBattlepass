package com.exanthiax.ecobattlepass.tasks

import com.exanthiax.ecobattlepass.api.events.PlayerTaskExpGainEvent
import com.exanthiax.ecobattlepass.api.giveTaskExperience
import com.exanthiax.ecobattlepass.api.hasCompletedQuest
import com.exanthiax.ecobattlepass.api.hasCompletedTask
import com.exanthiax.ecobattlepass.api.setCompletedTask
import com.exanthiax.ecobattlepass.api.setTaskProgress
import com.exanthiax.ecobattlepass.plugin
import com.exanthiax.ecobattlepass.quests.ActiveBattleQuest
import com.exanthiax.ecobattlepass.utils.InternalPlaceholders
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.util.formatEco
import com.willfp.libreforge.counters.Accumulator
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ActiveBattleTask(val config: Config, val quest: ActiveBattleQuest) {
    val parent = BattleTasks.getByID(config.getString("id"))!!

    val requiredXP = config.getDoubleFromExpression("xp")

    val xpGainMethods = parent.xpGainMethods.map { it.clone() }

    val completedKey = PersistentDataKey(
        plugin.createNamespacedKey("${parent.id}_${quest.parent.id}_task_completed"),
        PersistentDataKeyType.BOOLEAN,
        false
    )

    val progressKey = PersistentDataKey(
        plugin.createNamespacedKey("${parent.id}_${quest.parent.id}_progress"),
        PersistentDataKeyType.DOUBLE,
        0.0
    )

    var isBound = false

    fun isActive(player: Player): Boolean {
        return !player.hasCompletedTask(this) && !player.hasCompletedQuest(this.quest) && this.quest.category
            .isActive && this.quest.parent.isAllowed(player, quest.category.battlepass)
    }

    private val accumulator = object : Accumulator {
        override fun accept(player: Player, count: Double) {
            if (!this@ActiveBattleTask.isActive(player) || !isBound) {
                return
            }

            this@ActiveBattleTask.gainExperience(player, count)
        }
    }

    fun gainExperience(player: Player, count: Double) {
        val event = PlayerTaskExpGainEvent(player, this, count)
        Bukkit.getPluginManager().callEvent(event)

        if (!event.isCancelled) {
            player.giveTaskExperience(this, event.getAmount())
        }
    }

    fun reset(player: OfflinePlayer) {
        player.setCompletedTask(this, false)
        player.setTaskProgress(this, 0.0)
    }

    fun getDisplayItem(player: Player): ItemStack {
        return ItemStackBuilder(parent.testable)
            .setDisplayName(InternalPlaceholders.TaskPlaceholders.replace(parent.name, this, player))
            .addLoreLines(InternalPlaceholders.TaskPlaceholders.replaceAll(parent.lore, this, player))
            .build()
    }

    fun getIconDescription(player: Player): List<String> {
        val result = mutableListOf<String>()
        val tasksFormat = plugin.configYml.getStrings("quests-icon.tasks-format")

        for (line in tasksFormat) {
            when {
                line.contains("%task%", ignoreCase = true) -> {
                    result.add(InternalPlaceholders.TaskPlaceholders.replace(line, this, player))
                }

                line.contains("%task_lore%", ignoreCase = true) -> {
                    for (loreLine in this.parent.lore) {
                        result.add(
                            InternalPlaceholders.TaskPlaceholders.replace(
                                line.replace("%task_lore%", loreLine),
                                this,
                                player
                            )
                        )
                    }
                }

                else -> {
                    result.add(line)
                }
            }
        }

        return result.formatEco(player, true)
    }

    fun bind() {
        if (!isBound) {
            for (counter in xpGainMethods) {
                counter.bind(accumulator)
            }

            isBound = true
        }

    }

    fun unbind() {
        for (counter in xpGainMethods) {
            counter.unbind()
        }

        isBound = false
    }
}