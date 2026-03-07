package com.exanthiax.xbattlepass.commands

import com.exanthiax.xbattlepass.commands.give.GiveCommand
import com.exanthiax.xbattlepass.commands.reset.ResetCommand
import com.exanthiax.xbattlepass.plugin
import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.command.CommandSender

object XBattlePassCommand: PluginCommand(
    plugin,
    "xbattlepass",
    "xbattlepass.command.xbattlepass",
    false
) {
    init {
        this.addSubcommand(QuestsCommand)
            .addSubcommand(ReloadCommand)
            .addSubcommand(CompleteTaskCommand)
            .addSubcommand(ResetCommand)
            .addSubcommand(GiveCommand)
            .addSubcommand(TiersCommand)
            .addSubcommand(SetPremiumCommand)
    }

    override fun onExecute(sender: CommandSender, args: MutableList<String>) {
        sender.sendMessage(plugin.langYml.getMessage("invalid-command"))
    }
}