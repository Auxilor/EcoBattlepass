package ru.oftendev.xbattlepass.commands

import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.entity.Player
import ru.oftendev.xbattlepass.gui.BattleTiersGUI
import ru.oftendev.xbattlepass.gui.CategoriesGUI
import ru.oftendev.xbattlepass.plugin

object TiersCommand: PluginCommand(
    plugin,
    "tier",
    "xbattlepass.command.tier",
    true
) {
    override fun onExecute(sender: Player, args: MutableList<String>) {
        BattleTiersGUI.createAndOpen(sender)
    }
}