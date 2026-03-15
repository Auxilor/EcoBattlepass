package com.exanthiax.ecobattlepass.commands

import com.exanthiax.ecobattlepass.api.hasPremium
import com.exanthiax.ecobattlepass.api.setPremium
import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.exanthiax.ecobattlepass.commands.helpers.Messages
import com.exanthiax.ecobattlepass.commands.helpers.replacePlaceholders
import com.exanthiax.ecobattlepass.commands.helpers.resolveBattlePass
import com.exanthiax.ecobattlepass.commands.helpers.resolvePlayers
import com.exanthiax.ecobattlepass.plugin
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.sound.PlayableSound
import com.willfp.eco.util.formatEco
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

object SetPremiumCommand : PluginCommand(
    plugin,
    "setpremium",
    "ecobattlepass.command.setpremium",
    false
) {
    @Suppress("DEPRECATION")
    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (args.isEmpty()) {
            Messages.sendSetPremiumUsage(sender)
            return
        }

        val players = sender.resolvePlayers(args.getOrNull(0)) ?: return
        if (players.size > 1) {
            Messages.sendPlayerRequired(sender)
            return
        }
        val player = players.first()

        val pass = sender.resolveBattlePass(args.getOrNull(1)) ?: return

        val arg3 = args.getOrNull(2)?.lowercase()
        val arg4 = args.getOrNull(3)?.lowercase()

        val setToPremium = when (arg3) {
            null, "silent" -> true
            "true", "yes", "1" -> true
            else -> false
        }

        val silent = arg4 == "silent" || arg3 == "silent"

        val currentlyHasPremium = player.hasPremium(pass)

        if (setToPremium && currentlyHasPremium) {
            sender.sendMessage(
                Messages.getAlreadyPremium().replacePlaceholders(player, 0, pass)
            )
            return
        }
        if (!setToPremium && !currentlyHasPremium) {
            sender.sendMessage(
                Messages.getNotPremium().replacePlaceholders(player, 0, pass)
            )
            return
        }

        player.setPremium(pass, setToPremium)

        if (setToPremium) {
            PlayableSound.create(plugin.configYml.getSubsection("sound.premium-unlocked"))?.playTo(player)
        }

        val adminMessage = if (setToPremium) {
            Messages.getPremiumGiven()
        } else {
            Messages.getPremiumRemoved()
        }

        sender.sendMessage(
            adminMessage.replacePlaceholders(player, 0, pass)
        )

        val playerMessage = if (setToPremium) {
            Messages.getPremiumUnlocked()
        } else {
            Messages.getPremiumRevoked()
        }

        player.sendMessage(
            playerMessage.replacePlaceholders(player, 0, pass)
        )

        if (setToPremium && !silent) {
            Bukkit.broadcastMessage(
                Messages.getPremiumBroadcast()
                    .replacePlaceholders(player, 0, pass)
                    .formatEco(player)
            )
        }
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when (args.size) {
            1 -> StringUtil.copyPartialMatches(
                args[0],
                Bukkit.getOnlinePlayers().map { it.name },
                ArrayList()
            )

            2 -> StringUtil.copyPartialMatches(
                args[1],
                BattlePasses.values().map { it.id },
                ArrayList()
            )

            3 -> StringUtil.copyPartialMatches(
                args[2],
                listOf("true", "false"),
                ArrayList()
            )

            4 -> StringUtil.copyPartialMatches(
                args[3],
                listOf("silent"),
                ArrayList()
            )

            else -> emptyList()
        }
    }
}