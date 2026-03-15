package com.exanthiax.ecobattlepass.commands.give

import com.exanthiax.ecobattlepass.api.giveExactBPTiers
import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.exanthiax.ecobattlepass.commands.helpers.COMMON_AMOUNTS
import com.exanthiax.ecobattlepass.commands.helpers.Messages
import com.exanthiax.ecobattlepass.commands.helpers.replacePlaceholders
import com.exanthiax.ecobattlepass.commands.helpers.resolveBattlePass
import com.exanthiax.ecobattlepass.commands.helpers.resolvePlayers
import com.exanthiax.ecobattlepass.plugin
import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

object GiveTiersSubcommand : Subcommand(
    plugin,
    "tiers",
    "ecobattlepass.command.give.tiers",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        val players = sender.resolvePlayers(args.getOrNull(0)) ?: return
        val pass = sender.resolveBattlePass(args.getOrNull(1)) ?: return

        val amountString = args.getOrNull(2) ?: run {
            Messages.sendAmountRequired(sender)
            return
        }

        val amount = amountString.toIntOrNull() ?: run {
            Messages.sendInvalidAmount(sender)
            return
        }

        val baseGiven = Messages.getGivenTiers()
        val baseReceived = Messages.getReceivedTiers()

        val isAll = players.size > 1
        val displayName = if (isAll) "all players" else players.first().name

        for (player in players) {
            player.giveExactBPTiers(pass, amount)

            player.sendMessage(
                baseReceived.replacePlaceholders(player, amount, pass)
            )
        }

        sender.sendMessage(
            baseGiven.replacePlaceholders(
                player = players.first(),
                amount = amount,
                pass = pass
            ).replace("%playername%", displayName)
        )
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when (args.size) {
            1 -> StringUtil.copyPartialMatches(args[0], Bukkit.getOnlinePlayers().map { it.name } + "all", mutableListOf())
            2 -> StringUtil.copyPartialMatches(args[1], BattlePasses.values().map { it.id }, mutableListOf())
            3 -> StringUtil.copyPartialMatches(args[2], COMMON_AMOUNTS, mutableListOf())
            else -> emptyList()
        }
    }
}