package com.exanthiax.ecobattlepass.utils

import com.exanthiax.ecobattlepass.plugin
import com.willfp.eco.util.toNiceString

fun msToString(ms: Long): String {
    // Define constants
    val secondsPerMs = 0.001
    val secondsInMinute = 60
    val secondsInHour = 3600
    val secondsInDay = 86400

    // Convert ticks to total seconds
    val totalSeconds = ms * secondsPerMs

    // Calculate days, hours, minutes, and seconds
    val days = (totalSeconds / secondsInDay).toInt()
    val hours = ((totalSeconds % secondsInDay) / secondsInHour).toInt()
    val minutes = ((totalSeconds % secondsInHour) / secondsInMinute).toInt()
    val seconds = (totalSeconds % secondsInMinute).toInt()

    val lst = mutableListOf<String>()

    if (days > 0) {
        lst += plugin.configYml.getFormattedString("time-format.days")
            .replace("%value%", days.toNiceString())
    }
    if (hours > 0) {
        lst += plugin.configYml.getFormattedString("time-format.hours")
            .replace("%value%", hours.toNiceString())
    }
    if (minutes > 0) {
        lst += plugin.configYml.getFormattedString("time-format.minutes")
            .replace("%value%", minutes.toNiceString())
    }

    lst += plugin.configYml.getFormattedString("time-format.seconds")
        .replace("%value%", seconds.toNiceString())

    // Format the result as a string
    return lst.joinToString(plugin.configYml.getFormattedString("time-format.split"))
}
