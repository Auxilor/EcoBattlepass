package ru.oftendev.xbattlepass.categories

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.placeholder.PlayerPlaceholder
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import com.willfp.eco.core.registry.Registrable
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.toNiceString
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.oftendev.xbattlepass.api.hasCompletedQuest
import ru.oftendev.xbattlepass.plugin
import ru.oftendev.xbattlepass.quests.ActiveBattleQuest
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.min

class Category(private val _id: String, val config: Config): Registrable {
    init {
        PlayerlessPlaceholder(
            plugin,
            "category_${id}_start_date"
        ) {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            this.startDate.format(formatter)
        }.register()

        PlayerlessPlaceholder(
            plugin,
            "category_${id}_duration"
        ) {
            this.config.getInt("duration").toNiceString()
        }.register()

        PlayerlessPlaceholder(
            plugin,
            "category_${id}_reset_time"
        ) {
            this.resetTimer.toNiceString()
        }.register()
    }

    override fun getID(): String {
        return _id
    }

    val name = config.getString("name")

    val item = Items.lookup(config.getString("item"))

    val unformattedLore = config.getStrings("lore")

    val startDate = run {
        val dateTimeString = config.getString("start-date")
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        LocalDateTime.parse(dateTimeString, formatter)
    }

    val quests = config.getSubsections("quests").map {
        ActiveBattleQuest(it, this)
    }

    val endDate = run {
        val duration = config.getInt("duration")
        if (duration <= 0) {
            return@run null
        }
        startDate.plusMinutes(config.getInt("duration").toLong())
    }

    val resetTimer = config.getInt("reset-time").toLong() * 60000L

    fun getCompleted(player: Player): Int {
        return this.quests.count { player.hasCompletedQuest(it) }
    }

    fun getNextResetDate(): LocalDateTime {
        var current = startDate

        while(true) {
            current = current.plusNanos(resetTimer * 1_000_000L)
            if (current.isAfter(LocalDateTime.now())) {
                return current
            }
        }
    }

    fun getDisplayItem(player: Player): ItemStack {
        return ItemStackBuilder(item.item.clone())
            .setDisplayName(name.replace("%completed%", getCompleted(player).toString())
                .replace("%total%", this.quests.size.toString()).formatEco(formatPlaceholders = true))
            .addLoreLines(unformattedLore
                .map {
                    it.replace("%completed%", getCompleted(player).toString())
                        .replace("%total%", this.quests.size.toString())
                }
                .formatEco(formatPlaceholders = true))
            .build()
    }

    fun getDisplayableMs(): Long {
        return if (this.isActive) {
            if (resetTimer > 0) {
                val nextDate = min(getNextResetDate().toInstant(OffsetDateTime.now().offset).toEpochMilli(),
                    endDate?.toInstant(OffsetDateTime.now().offset)?.toEpochMilli() ?: Long.MAX_VALUE)
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
            if (getNextResetDate() < (endDate ?: LocalDateTime.now())) {
                "reset"
            } else if (endDate != null) {
                "end"
            } else "none"
        } else "start"
    }

    fun isToReset(): Boolean {
        if (resetTimer <= 0) {
            return false
        }

        val nextReset = getNextResetDate()

        return nextReset.isBefore(LocalDateTime.now()) || nextReset == LocalDateTime.now()
    }

    val isActive: Boolean
        get() = LocalDateTime.now().isAfter(startDate) && (endDate == null || LocalDateTime.now().isBefore(endDate))

    var consideredActive: Boolean = isActive
}