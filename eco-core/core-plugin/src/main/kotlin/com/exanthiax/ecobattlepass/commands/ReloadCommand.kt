package com.exanthiax.ecobattlepass.commands

import com.exanthiax.ecobattlepass.plugin
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.util.NumberUtils
import com.willfp.eco.util.StringUtils
import org.bukkit.command.CommandSender

object ReloadCommand: PluginCommand(
    plugin,
    "reload",
    "ecobattlepass.command.reload",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender.sendMessage(
            plugin.langYml.getMessage("reloaded", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                .replace("%time%", NumberUtils.format(plugin.reloadWithTime().toDouble()))
        )
    }
}