package com.exanthiax.ecobattlepass.api

import com.willfp.eco.core.cache.EcoCache
import org.bukkit.entity.Player
import java.time.Duration
import com.willfp.eco.util.NumericalPermissions

private val expMultiplierCache = EcoCache.builder<Player, Double>()
    .expireAfterWrite(Duration.ofSeconds(10)).build {
        it.cacheBPExperienceMultiplier()
    }

val Player.bpExperienceMultiplier: Double
    get() = expMultiplierCache.get(this) { it.cacheBPExperienceMultiplier() }

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
    // Delegates to eco so the four copies of this loop cannot drift apart again. Two
    // behaviour fixes come with it: a permission explicitly set to false no longer counts,
    // and a negative value is honoured rather than lost to a `Double.MIN_VALUE` seed - which
    // is the smallest *positive* double, so `.-50` used to resolve to roughly zero.
    //
    // The node itself stays this plugin's own; eco supplies the arithmetic, never a prefix.
    return NumericalPermissions.highest(
        this.effectivePermissions.filter { it.value }.map { it.permission },
        permission,
        default
    )
}