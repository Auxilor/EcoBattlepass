package ru.oftendev.xbattlepass.commands

import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.entity.Player
import ru.oftendev.xbattlepass.gui.CategoriesGUI
import ru.oftendev.xbattlepass.plugin

object QuestsCommand: PluginCommand(
    plugin,
    "quests",
    "xbattlepass.command.quests",
    true
) {
    override fun onExecute(sender: Player, args: MutableList<String>) {
        CategoriesGUI(sender).open()
    }
}