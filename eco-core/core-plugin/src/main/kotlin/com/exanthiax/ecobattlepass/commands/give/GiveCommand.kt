package com.exanthiax.ecobattlepass.commands.give

import com.exanthiax.ecobattlepass.commands.helpers.Messages
import com.exanthiax.ecobattlepass.plugin
import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

object GiveCommand : PluginCommand(
    plugin,
    "give",
    "ecobattlepass.command.give",
    false
) {
    init {
        addSubcommand(GiveXPSubcommand)
        addSubcommand(GiveTiersSubcommand)
        addSubcommand(GiveTaskXPSubcommand)
    }


    override fun onExecute(sender: CommandSender, args: List<String>) {
        Messages.sendGiveUsage(sender)
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        val subcommands = listOf("xp", "tiers", "taskxp")

        return if (args.isEmpty()) {
            subcommands
        } else {
            StringUtil.copyPartialMatches(args[0], subcommands, mutableListOf())
        }
    }
}