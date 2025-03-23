package ru.oftendev.xbattlepass.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.util.NumberUtils
import com.willfp.eco.util.StringUtils
import org.bukkit.command.CommandSender
import ru.oftendev.xbattlepass.plugin

object ReloadCommand: PluginCommand(
    plugin,
    "reload",
    "xbattlepass.command.reload",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender.sendMessage(
            plugin.langYml.getMessage("reloaded", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                .replace("%time%", NumberUtils.format(plugin.reloadWithTime().toDouble()))
        )
    }
}