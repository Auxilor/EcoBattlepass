package ru.oftendev.xbattlepass.tasks

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.toNiceString
import com.willfp.libreforge.counters.Accumulator
import com.willfp.libreforge.counters.Counters
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.inventory.ItemStack
import ru.oftendev.xbattlepass.api.events.PlayerTaskExpGainEvent
import ru.oftendev.xbattlepass.api.giveTaskExperience
import ru.oftendev.xbattlepass.api.hasCompletedQuest
import ru.oftendev.xbattlepass.api.hasCompletedTask
import ru.oftendev.xbattlepass.api.taskProgress
import ru.oftendev.xbattlepass.plugin
import ru.oftendev.xbattlepass.quests.ActiveBattleQuest
import ru.oftendev.xbattlepass.quests.BattleQuest

class ActiveBattleTask(val config: Config, val quest: ActiveBattleQuest) {
    val parent = BattleTasks.getByID(config.getString("id"))!!

    val requiredXP = config.getDouble("xp")

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
            .isActive && this.quest.parent.isAllowed(player)
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

    fun getDisplayItem(player: Player): ItemStack {
        return ItemStackBuilder(parent.testable)
            .setDisplayName(parent.name
                .replace("%current%", player.taskProgress(this).toNiceString())
                .replace("%required%", this.requiredXP.toNiceString())
                .formatEco(formatPlaceholders = true))
            .addLoreLines(parent.lore
                .map {
                    it.replace("%current%", player.taskProgress(this).toNiceString())
                        .replace("%required%", this.requiredXP.toNiceString())
                }
                .formatEco(formatPlaceholders = true))
            .build()
    }

    fun getIconDescription(player: Player): List<String> {
        val result = mutableListOf<String>()
        val tasksFormat = plugin.configYml.getStrings("quests-icon.tasks-format")

        for (line in tasksFormat) {
            if (line.contains("%task_name%", true)) {
                result.add(line.replace("%task_name%", this.parent.name.replace("%current%",
                    player.taskProgress(this).toNiceString())
                    .replace("%required%", this.requiredXP.toNiceString())))
            } else if (line.contains("%task_lore%", true)) {
                for (loreLine in this.parent.lore) {
                    result.add(line.replace("%task_lore%", loreLine.replace("%current%",
                        player.taskProgress(this).toNiceString())
                        .replace("%required%", this.requiredXP.toNiceString())))
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