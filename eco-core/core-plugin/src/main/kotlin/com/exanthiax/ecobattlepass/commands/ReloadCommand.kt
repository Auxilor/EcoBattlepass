package com.exanthiax.ecobattlepass.commands

import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.exanthiax.ecobattlepass.categories.Categories
import com.exanthiax.ecobattlepass.plugin
import com.exanthiax.ecobattlepass.quests.BattleQuests
import com.exanthiax.ecobattlepass.rewards.Rewards
import com.exanthiax.ecobattlepass.tasks.BattleTasks
import com.willfp.eco.core.Prerequisite
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.util.NumberUtils
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.toNiceString
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

object ReloadCommand: PluginCommand(
    plugin,
    "reload",
    "ecobattlepass.command.reload",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (Prerequisite.HAS_FOLIA.isMet && !Bukkit.isGlobalTickThread()) {
            plugin.scheduler.global().run { onExecute(sender, args) }
            return
        }

        sender.sendMessage(
            plugin.langYml.getMessage("reloaded", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                .replace("%time%", plugin.reloadWithTime().toNiceString())
                .replace("%count%", BattlePasses.values().size.toString())
        )
    }
}