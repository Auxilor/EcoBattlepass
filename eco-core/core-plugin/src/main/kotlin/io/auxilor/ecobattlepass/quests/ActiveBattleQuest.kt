package io.auxilor.ecobattlepass.quests

import io.auxilor.ecobattlepass.api.setCompletedQuest
import io.auxilor.ecobattlepass.categories.Category
import io.auxilor.ecobattlepass.plugin
import io.auxilor.ecobattlepass.tasks.ActiveBattleTask
import io.auxilor.ecobattlepass.utils.msToString
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.ServerProfile
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.util.formatEco
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

class ActiveBattleQuest(val config: Config, val category: Category) {
    val parent = BattleQuests.getByID(config.getString("id"))!!

    val completedKey = PersistentDataKey(
        plugin.createNamespacedKey("${parent.id}_${category.id}_quest_completed"),
        PersistentDataKeyType.BOOLEAN,
        false
    )

    private val savedTasksKey = PersistentDataKey(
        plugin.createNamespacedKey("${parent.id}_${category.id}_selected_tasks"),
        PersistentDataKeyType.STRING_LIST,
        emptyList()
    )

    private val _tasks = parent.tasks.map { it.toActiveBattleTask(this) }

    private val preparedById: Map<String, PreparedBattleTask> =
        parent.tasks.associateBy { it.config.getString("id") }

    private fun pickTasks(): List<ActiveBattleTask> {
        return parent.tasks.shuffled().take(parent.taskAmount).map { it.toActiveBattleTask(this) }
    }

    private fun loadSavedTasks(): List<ActiveBattleTask>? {
        val ids = ServerProfile.load().read(savedTasksKey)
        if (ids.isEmpty()) return null
        val matched = ids.mapNotNull { preparedById[it] }
        if (matched.size != ids.size || matched.size != parent.taskAmount) return null
        return matched.map { it.toActiveBattleTask(this) }
    }

    private fun saveTasks(picked: List<ActiveBattleTask>) {
        ServerProfile.load().write(savedTasksKey, picked.map { it.parent.id })
    }

    var tasks: List<ActiveBattleTask> = run {
        val loaded = loadSavedTasks()
        if (loaded != null) {
            loaded
        } else {
            val picked = pickTasks()
            saveTasks(picked)
            picked
        }
    }
        private set

    fun regenerate() {
        for (task in tasks) {
            task.unbind()
        }

        val picked = pickTasks()
        tasks = picked
        saveTasks(picked)

        if (category.isActive) {
            for (task in tasks) {
                task.bind()
            }
        }
    }

    fun getFormattedName(player: Player): String {
        return plugin.configYml.getString("quests-icon.name").replace(
            "%quest_name%", parent.displayName
        ).formatEco(player, true)
    }

    fun reset(player: OfflinePlayer) {
        player.setCompletedQuest(this, false)
        this._tasks.forEach {
            it.reset(player)
        }
    }

    fun getFormattedLore(player: Player): List<String> {
        val result = mutableListOf<String>()
        val iconLore = plugin.configYml.getStrings("quests-icon.lore")

        for (line in iconLore) {
            if (line.contains("%quest_description%")) {
                for (loreLine in this.parent.displayLore) {
                    result.add(line.replace("%quest_description%", loreLine))
                }
            } else if (line.contains("%quest_tasks%")) {
                val tasksSeparator = plugin.configYml.getStrings("quests-icon.tasks-separator")

                for (task in this.tasks) {
                    result.addAll(task.getIconDescription(player))
                    result.addAll(tasksSeparator.formatEco(player, true))
                }
            } else if (line.contains("%quest_tier%")) {
                result.add(line.replace("%quest_tier%", this.parent.formattedName))
            } else if (line.contains("%quest_timer%")) {
                val key = this.category.getDisplayableStatusKey()
                val formattedTime = msToString(this.category.getDisplayableMs())
                result.add(
                    line.replace(
                        "%quest_timer%", plugin.configYml
                            .getString("quests-icon.timer-format.$key")
                            .replace("%time%", formattedTime)
                    ),
                )
            } else {
                result.add(line.formatEco(player, true))
            }
        }

        return result.formatEco(player, true)
    }
}
