package com.exanthiax.ecobattlepass.commands.reset

import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.exanthiax.ecobattlepass.commands.helpers.Messages
import com.exanthiax.ecobattlepass.commands.helpers.replacePlaceholders
import com.exanthiax.ecobattlepass.commands.helpers.resolveBattlePass
import com.exanthiax.ecobattlepass.commands.helpers.resolvePlayers
import com.exanthiax.ecobattlepass.plugin
import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

object ResetBattlepassSubcommand : Subcommand(
    plugin,
    "battlepass",
    "ecobattlepass.command.reset.battlepass",
    false,
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        val players = sender.resolvePlayers(args.getOrNull(0)) ?: return
        val pass = sender.resolveBattlePass(args.getOrNull(1)) ?: return

        val isAll = players.size > 1
        val displayName = if (isAll) "all players" else players.first().name

        for (player in players) {
            pass.reset(player)
        }

        val baseMessage = Messages.getResetPlayer()

        sender.sendMessage(
            baseMessage.replacePlaceholders(
                player = players.first(),
                amount = 0,
                pass = pass
            ).replace("%playername%", displayName)
        )
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when (args.size) {
            1 -> StringUtil.copyPartialMatches(
                args[0],
                Bukkit.getOnlinePlayers().map { it.name } + "all",
                mutableListOf()
            )
            2 -> StringUtil.copyPartialMatches(
                args[1],
                BattlePasses.values().map { it.id },
                mutableListOf()
            )
            else -> emptyList()
        }
    }
}