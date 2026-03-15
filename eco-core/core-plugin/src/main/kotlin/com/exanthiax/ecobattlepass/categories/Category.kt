package com.exanthiax.ecobattlepass.categories

import com.exanthiax.ecobattlepass.api.hasCompletedQuest
import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.exanthiax.ecobattlepass.plugin
import com.exanthiax.ecobattlepass.quests.ActiveBattleQuest
import com.exanthiax.ecobattlepass.utils.InternalPlaceholders
import com.exanthiax.ecobattlepass.utils.msToString
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import com.willfp.eco.core.registry.Registrable
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.min

class Category(private val _id: String, val config: Config) : Registrable {
    init {
        PlayerlessPlaceholder(plugin, "category_${id}_start_date") {
            val pattern = plugin.configYml.getString("date-format")
            val formatter = DateTimeFormatter.ofPattern(pattern)
            this.startDate.format(formatter)
        }.register()

        PlayerlessPlaceholder(plugin, "category_${id}_start_timer") {
            val millisLeft = startDate.atZone(ZoneId.systemDefault()).toInstant()
                .toEpochMilli() - System.currentTimeMillis()
            if (millisLeft <= 0) {
                plugin.langYml.getFormattedString("category-in-progress")
            } else {
                msToString(millisLeft)
            }
        }.register()

        PlayerlessPlaceholder(plugin, "category_${id}_end_date") {
            val pattern = plugin.configYml.getString("date-format")
            val formatter = DateTimeFormatter.ofPattern(pattern)
            this.endDate?.format(formatter)
        }.register()

        PlayerlessPlaceholder(plugin, "category_${id}_end_timer") {
            val duration = config.getInt("duration")
            if (duration == -1) {
                plugin.langYml.getFormattedString("infinity")
            } else {
                val millisLeft = endDate!!.atZone(ZoneId.systemDefault()).toInstant()
                    .toEpochMilli() - System.currentTimeMillis()
                if (millisLeft <= 0) {
                    plugin.langYml.getFormattedString("category-expired")
                } else {
                    msToString(millisLeft)
                }
            }
        }.register()

        PlayerlessPlaceholder(plugin, "category_${id}_reset_timer") {
            val resetTime = config.getInt("reset-time")
            if (resetTime <= 0) {
                plugin.langYml.getFormattedString("infinity")
            } else {
                val nextReset = this.getNextResetDate()
                if (nextReset != null) {
                    val millisLeft = nextReset.atZone(ZoneId.systemDefault()).toInstant()
                        .toEpochMilli() - System.currentTimeMillis()
                    msToString(millisLeft.coerceAtLeast(0))
                } else {
                    msToString(0)
                }
            }
        }.register()
    }

    override fun getID(): String = _id

    val battlepass: BattlePass
        get() = BattlePasses.getByID(config.getString("battlepass"))!!

    val name = config.getString("name")
    val title = config.getString("gui-title")
    val item = Items.lookup(config.getString("item"))
    val unformattedLore = config.getStrings("lore")

    val startDate: LocalDateTime = run {
        val dateTimeString = config.getString("start-date")
        val pattern = plugin.configYml.getString("date-format")
        val formatter = DateTimeFormatter.ofPattern(pattern)
        LocalDateTime.parse(dateTimeString, formatter)
    }

    val quests = config.getSubsections("quests").map { ActiveBattleQuest(it, this) }

    val endDate = run {
        val duration = config.getInt("duration")
        if (duration <= 0) return@run null
        startDate.plusMinutes(duration.toLong())
    }

    val resetTimer = config.getInt("reset-time").toLong() * 60000L

    fun getCompleted(player: Player): Int = this.quests.count { player.hasCompletedQuest(it) }

    fun getNextResetDate(): LocalDateTime? {
        if (resetTimer <= 0) return null
        var current = startDate
        while (true) {
            current = current.plusNanos(resetTimer * 1_000_000L)
            if (current.isAfter(LocalDateTime.now())) return current
        }
    }

    fun getDisplayItem(player: Player): ItemStack {
        //val key = this.getDisplayableStatusKey()
        //val formattedTime = msToString(this.getDisplayableMs())

        return ItemStackBuilder(item.item.clone())
            .setDisplayName(InternalPlaceholders.CategoryPlaceholders.replace(name, this, player))
            .addLoreLines(InternalPlaceholders.CategoryPlaceholders.replaceAll(unformattedLore, this, player))
            .build()
    }

    fun getDisplayableMs(): Long {
        return if (this.isActive) {
            if (resetTimer > 0) {
                val nextDate = min(
                    getNextResetDate()!!.toInstant(OffsetDateTime.now().offset).toEpochMilli(),
                    endDate?.toInstant(OffsetDateTime.now().offset)?.toEpochMilli() ?: Long.MAX_VALUE
                )
                nextDate - System.currentTimeMillis()
            } else {
                val nextDate = endDate?.toInstant(OffsetDateTime.now().offset)?.toEpochMilli() ?: Long.MAX_VALUE
                nextDate - System.currentTimeMillis()
            }
        } else {
            val nextDate = startDate.toInstant(OffsetDateTime.now().offset).toEpochMilli()
            nextDate - System.currentTimeMillis()
        }
    }

    fun getDisplayableStatusKey(): String {
        return if (this.isActive) {
            val resetDate = getNextResetDate()
            if (resetDate != null && (endDate == null || resetDate.isBefore(endDate))) "reset"
            else if (endDate != null) "end"
            else "none"
        } else "start"
    }

    fun isToReset(): Boolean {
        if (resetTimer <= 0) return false
        val nextReset = getNextResetDate()!!
        return nextReset.isBefore(LocalDateTime.now()) || nextReset == LocalDateTime.now()
    }

    fun reset() {
        for (player in Bukkit.getOfflinePlayers()) {
            for (quest in this.quests) {
                quest.reset(player)
            }
        }
    }

    val isActive: Boolean
        get() = this.battlepass.isActive && LocalDateTime.now().isAfter(startDate) &&
                (endDate == null || LocalDateTime.now().isBefore(endDate))

    var consideredActive: Boolean = isActive
}