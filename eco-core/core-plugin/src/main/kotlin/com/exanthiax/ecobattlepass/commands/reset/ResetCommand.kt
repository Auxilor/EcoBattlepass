package com.exanthiax.ecobattlepass.commands.reset

import com.exanthiax.ecobattlepass.commands.helpers.Messages
import com.exanthiax.ecobattlepass.plugin
import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

object ResetCommand : PluginCommand(
    plugin,
    "reset",
    "ecobattlepass.command.reset",
    false
) {
    init {
        addSubcommand(ResetBattlepassSubcommand)
        addSubcommand(ResetTaskSubcommand)
    }

    override fun onExecute(sender: CommandSender, args: List<String>) {
        Messages.sendResetUsage(sender)
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        val subcommands = listOf("task", "battlepass")

        return if (args.isEmpty()) {
            subcommands
        } else {
            StringUtil.copyPartialMatches(args[0], subcommands, mutableListOf())
        }
    }
}