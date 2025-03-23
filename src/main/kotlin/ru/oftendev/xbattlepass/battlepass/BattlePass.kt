package ru.oftendev.xbattlepass.battlepass

import com.willfp.eco.core.Eco
import com.willfp.eco.core.data.PlayerProfile
import com.willfp.eco.core.data.Profile
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.util.evaluateExpression
import com.willfp.eco.util.toNiceString
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import ru.oftendev.xbattlepass.api.bpPassExp
import ru.oftendev.xbattlepass.api.bpTier
import ru.oftendev.xbattlepass.api.hasReceivedTier
import ru.oftendev.xbattlepass.categories.Categories
import ru.oftendev.xbattlepass.plugin
import ru.oftendev.xbattlepass.quests.ActiveBattleQuest
import ru.oftendev.xbattlepass.tasks.ActiveBattleTask

object BattlePass {
    val tiers = mutableListOf<BPTier>()

    val maxLevel: Int
        get() = plugin.battlePassYml.getInt("battlepass.max-tier")

    val activeTasks: List<ActiveBattleTask>
        get() = Categories.values().filter { it.isActive }
            .map { category -> category.quests.map { it.tasks }.flatten() }.flatten()

    private lateinit var xpFormula: String

    fun updateTaskBindings() {
        Categories.values().forEach { category -> category.quests.forEach { quest -> quest.tasks.forEach {
            it.unbind()
        } } }

        activeTasks.forEach { task -> task.unbind() }

        activeTasks.forEach { task -> task.bind() }

        plugin.logger.info("Rebound ${activeTasks.size} tasks")
    }

    fun getTier(level: Int): BPTier? {
        val exact = tiers.firstOrNull {
            it.number == level
        }

        return exact ?: tiers.firstOrNull { it.number >= level }
    }

    fun getActiveQuest(id: String): ActiveBattleQuest? {
        for (category in Categories.values()) {
            category.quests.forEach { quest ->
                if (quest.parent.id.equals(id, true)) return quest
            }
        }

        return null
    }

    fun update() {
        tiers.clear()
        tiers.addAll(
            plugin.battlePassYml.getSubsections("tiers").map { BPTier(it) }
        )

        val registeredTiers = tiers.map { it.number }

        for (i in (1..maxLevel).subtract(registeredTiers.toSet())) {
            tiers.add(
                BPTier(i)
            )
        }

        xpFormula = plugin.battlePassYml.getString("battlepass.xp-formula")

        plugin.logger.info("Registered ${tiers.size} tiers (${tiers.filter { it.transient }.size} transient)")
    }

    fun tickUpdates() {
        for (category in Categories.values()) {
            if (category.isActive && !category.consideredActive) {
                updateTaskBindings()
            } else if (!category.isActive && category.consideredActive) {
                updateTaskBindings()
            }
        }
    }

    fun getRewardsFormat(tierType: TierType): String {
        val key = when (tierType) {
            TierType.PREMIUM -> "premium"
            TierType.FREE -> "free"
        }

        return plugin.configYml.getFormattedString("tiers-gui.buttons.$key-rewards-format")
    }

    /**
     * Get the XP required to reach the next level, if currently at [level].
     */
    fun getExpForLevel(level: Int): Double {
        return if (level <= 0) {
            0.0
        } else evaluateExpression(
            xpFormula.replace("%level%", level.toString()),
        )
    }

    fun getProgress(player: Player): Double {
        return player.bpPassExp / getExpForLevel(player.bpTier + 1)
    }

    fun getFormattedProgress(player: Player): String {
        return (getProgress(player) * 100.0).toNiceString()
    }

    fun getFormattedRequired(player: Player): String {
        return getFormattedExpForLevel(player.bpTier + 1)
    }

    fun getFormattedExpForLevel(level: Int): String {
        val required = getExpForLevel(level)
        return if (required.isInfinite()) {
            plugin.langYml.getFormattedString("infinity")
        } else {
            required.toNiceString()
        }
    }

    fun getClaimable(player: Player): Int {
        return tiers.filter {
            player.bpTier >= it.number && !player.hasReceivedTier(it.number)
        }.size
    }

    fun resetAll() {
        for (offlinePlayer in Bukkit.getOfflinePlayers()) {
            reset(offlinePlayer)
        }
    }

    fun reset(player: OfflinePlayer) {
        val profile = player.profile
        val keys = Eco.get().registeredPersistentDataKeys.filter { it.key.namespace == "xbattlepass" }
        for (persistentDataKey in keys) {
            persistentDataKey.type
            writeToProfile(profile, persistentDataKey)
        }
    }

    private fun <T : Any> writeToProfile(profile: Profile, key: PersistentDataKey<T>) {
        profile.write(key, key.defaultValue)
    }
}