package ru.oftendev.xbattlepass.commands

import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ru.oftendev.xbattlepass.gui.BattlePassGUI
import ru.oftendev.xbattlepass.gui.BattleTiersGUI
import ru.oftendev.xbattlepass.plugin

object XBattlePassCommand: PluginCommand(
    plugin,
    "xbattlepass",
    "xbattlepass.command.xbattlepass",
    false
) {
    init {
        this.addSubcommand(
            QuestsCommand
        ).addSubcommand(
            ReloadCommand
        ).addSubcommand(
            ResetCommand
        ).addSubcommand(
            GiveCommand
        ).addSubcommand(
            TiersCommand
        )
    }

    override fun onExecute(sender: CommandSender, args: MutableList<String>) {
        if (args.isEmpty() && sender is Player) {
            BattlePassGUI.createAndOpen(sender)
        } else sender.sendMessage(plugin.langYml.getMessage("invalid-command"))
    }

    override fun getAliases(): MutableList<String> {
        return mutableListOf("bp", plugin.configYml.getString("open-command"))
    }
}