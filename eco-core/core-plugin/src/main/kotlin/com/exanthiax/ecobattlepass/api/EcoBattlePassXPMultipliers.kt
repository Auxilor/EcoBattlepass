package com.exanthiax.ecobattlepass.api

import com.github.benmanes.caffeine.cache.Caffeine
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit
import kotlin.math.max

private val expMultiplierCache = Caffeine.newBuilder()
    .expireAfterWrite(10, TimeUnit.SECONDS).build<Player, Double> {
        it.cacheBPExperienceMultiplier()
    }

val Player.bpExperienceMultiplier: Double
    get() = expMultiplierCache.get(this)

private fun Player.cacheBPExperienceMultiplier(): Double {
    if (this.hasPermission("ecobattlepass.xpmultiplier.quadruple")) {
        return 4.0
    }

    if (this.hasPermission("ecobattlepass.xpmultiplier.triple")) {
        return 3.0
    }

    if (this.hasPermission("ecobattlepass.xpmultiplier.double")) {
        return 2.0
    }

    if (this.hasPermission("ecobattlepass.xpmultiplier.50percent")) {
        return 1.5
    }

    return 1 + getNumericalPermission("ecobattlepass.xpmultiplier", 0.0) / 100
}

fun Player.getNumericalPermission(permission: String, default: Double): Double {
    var highest: Double? = null

    for (permissionAttachmentInfo in this.effectivePermissions) {
        val perm = permissionAttachmentInfo.permission
        if (perm.startsWith(permission)) {
            val found = perm.substring(perm.lastIndexOf(".") + 1).toDoubleOrNull() ?: continue
            highest = max(highest ?: Double.MIN_VALUE, found)
        }
    }

    return highest ?: default
}