package com.exanthiax.ecobattlepass.battlepass

import com.exanthiax.ecobattlepass.api.getPassExp
import com.exanthiax.ecobattlepass.api.getTier
import com.exanthiax.ecobattlepass.api.hasReceivedTier
import com.exanthiax.ecobattlepass.categories.Categories
import com.exanthiax.ecobattlepass.categories.Category
import com.exanthiax.ecobattlepass.commands.dynamic.DynamicPassCommand
import com.exanthiax.ecobattlepass.plugin
import com.exanthiax.ecobattlepass.quests.ActiveBattleQuest
import com.exanthiax.ecobattlepass.tiers.BPTier
import com.exanthiax.ecobattlepass.utils.ReceivedTierState
import com.willfp.eco.core.Eco
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.Profile
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.placeholder.PlayerDynamicPlaceholder
import com.willfp.eco.core.placeholder.PlayerPlaceholder
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import com.willfp.eco.core.registry.Registrable
import com.willfp.eco.util.evaluateExpression
import com.willfp.eco.util.formatWithCommas
import com.willfp.eco.util.toNiceString
import com.willfp.eco.util.toNumeral
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

class BattlePass(private val _id: String, val config: Config): Registrable {
    init {
        PlayerlessPlaceholder(plugin, "${_id}_max_tiers") {
            this.maxLevel.toString()
        }.register()

        PlayerlessPlaceholder(plugin, "${_id}_max_tiers_numeral") {
            this.maxLevel.toNumeral()
        }.register()

        PlayerDynamicPlaceholder(plugin, Pattern.compile("tier_state_${_id}_\\d+$")) { string, player ->
            val rTier = string.split("_").last().toIntOrNull()
                ?: return@PlayerDynamicPlaceholder "Invalid tier ${string.split("_").last()}"
            player.hasReceivedTier(this, rTier).toString()
        }.register()

        PlayerPlaceholder(plugin, "${_id}_pass_type") { player ->
            plugin.langYml.getString(
                if (player.hasPermission(premiumPerm)) "pass-type.premium" else "pass-type.free"
            )
        }.register()

        PlayerPlaceholder(plugin, "tier_${_id}") {
            player -> player.getTier(this).toNiceString()
        }.register()

        PlayerPlaceholder(plugin, "tier_${_id}_numeral") {
            player -> player.getTier(this).toNumeral()
        }.register()

        PlayerPlaceholder(plugin, "xp_required_${_id}") {
            player -> getFormattedRequired(player)
        }.register()

        PlayerPlaceholder(plugin, "xp_required_${_id}_formatted") {
            player -> getFormattedRequired(player).toDouble().formatWithCommas()
        }.register()

        PlayerPlaceholder(plugin, "xp_${_id}") {
            player -> player.getPassExp(this).toNiceString()
        }.register()

        PlayerPlaceholder(plugin, "xp_${_id}_formatted") {
            player -> player.getPassExp(this).formatWithCommas()
        }.register()

        PlayerPlaceholder(plugin, "claimable_${_id}") {
            player -> getClaimable(player).toNiceString()
        }.register()

        PlayerPlaceholder(plugin, "${_id}_percentage_progress") {
            player -> getFormattedProgress(player)
        }.register()
    }

    val tierKey = PersistentDataKey(
        plugin.createNamespacedKey("bp_tier_$_id"),
        PersistentDataKeyType.INT, 0
    )

    val passExpKey = PersistentDataKey(
        plugin.createNamespacedKey("bp_pass_exp_$_id"),
        PersistentDataKeyType.DOUBLE, 0.0
    )

    val receivedTiersKey = PersistentDataKey(
        plugin.createNamespacedKey("bp_tiers_received_$_id"),
        PersistentDataKeyType.STRING_LIST, emptyList()
    )

    override fun getID(): String {
        return this._id
    }

    val name = config.getFormattedString("name")

    val premiumPerm = config.getFormattedString("battlepass.premium-permission")

    val openCommand = DynamicPassCommand(this, config.getString("battlepass.command")).apply {
        this.register()
    }

    val isActive: Boolean
        get() = LocalDateTime.now().isAfter(startDate) && LocalDateTime.now().isBefore(endDate)

    val startDate = run {
        val dateTimeString = config.getString("battlepass.battlepass-start")
        val pattern = plugin.configYml.getString("date-format")
        val formatter = DateTimeFormatter.ofPattern(pattern)
        LocalDateTime.parse(dateTimeString, formatter)
    }

    val endDate = run {
        val dateTimeString = config.getString("battlepass.battlepass-end")
        val pattern = plugin.configYml.getString("date-format")
        val formatter = DateTimeFormatter.ofPattern(pattern)
        LocalDateTime.parse(dateTimeString, formatter)
    }

    val tiers = config.getSubsections("tiers").map { BPTier(it, this) }.toMutableList().apply {
        val registeredTiers = this.map { tier -> tier.number }

        for (i in (1..maxLevel).subtract(registeredTiers.toSet())) {
            this.add(
                BPTier(i, this@BattlePass)
            )
        }

        plugin.logger.info("Registered ${this.size} tiers (${this.filter { it.transient }.size} transient)" +
                " for pass ${this@BattlePass.name}")
    }

    val xpFormula = config.getString("battlepass.xp-formula")

    val maxLevel: Int
        get() = config.getInt("battlepass.max-tier")

    val categories: List<Category>
        get() = Categories.values().filter { it.battlepass == this }
            .sortedWith(compareBy<Category>{ it.config.getInt("priority") }.thenBy { it.id })

    fun getExpForLevel(level: Int): Double {
        return if (level <= 0) {
            0.0
        } else evaluateExpression(
            xpFormula.replace("%level%", level.toString()),
        )
    }

    fun getProgress(player: Player): Double {
        return player.getPassExp(this) / getExpForLevel(player.getTier(this) + 1)
    }

    fun getFormattedProgress(player: Player): String {
        return (getProgress(player) * 100.0).toNiceString()
    }

    fun getFormattedRequired(player: Player): String {
        return getFormattedExpForLevel(player.getTier(this) + 1)
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
            player.getTier(this) >= it.number && player.hasReceivedTier(this, it.number) != ReceivedTierState.RECEIVED
        }.size
    }

    fun resetAll() {
        for (offlinePlayer in Bukkit.getOfflinePlayers()) {
            reset(offlinePlayer)
        }
    }

    @Suppress("UnstableApiUsage")
    fun reset(player: OfflinePlayer) {
        val profile = player.profile
        val keys = Eco.get().registeredPersistentDataKeys.filter { it.key.namespace == "ecobattlepass" }
        for (persistentDataKey in keys) {
            persistentDataKey.type
            writeToProfile(profile, persistentDataKey)
        }
    }

    fun getTier(level: Int): BPTier? {
        val exact = tiers.firstOrNull {
            it.number == level
        }

        return exact ?: tiers.firstOrNull { it.number >= level }
    }

    fun getActiveQuest(id: String): ActiveBattleQuest? {
        for (category in categories) {
            category.quests.forEach { quest ->
                if (quest.parent.id.equals(id, true)) return quest
            }
        }

        return null
    }

    private fun <T : Any> writeToProfile(profile: Profile, key: PersistentDataKey<T>) {
        profile.write(key, key.defaultValue)
    }
}