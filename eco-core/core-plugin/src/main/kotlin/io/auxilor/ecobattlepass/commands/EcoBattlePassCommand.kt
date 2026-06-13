package io.auxilor.ecobattlepass.commands

import io.auxilor.ecobattlepass.commands.give.GiveCommand
import io.auxilor.ecobattlepass.commands.reset.ResetCommand
import io.auxilor.ecobattlepass.plugin
import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.command.CommandSender

object EcoBattlePassCommand: PluginCommand(
    plugin,
    "ecobattlepass",
    "ecobattlepass.command.ecobattlepass",
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