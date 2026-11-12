package com.exanthiax.ecobattlepass.commands

import com.exanthiax.ecobattlepass.api.hasPremium
import com.exanthiax.ecobattlepass.api.setPremium
import com.exanthiax.ecobattlepass.battlepass.BattlePass
import com.exanthiax.ecobattlepass.commands.helpers.Messages
import com.exanthiax.ecobattlepass.commands.helpers.replacePlaceholders
import com.exanthiax.ecobattlepass.runOwned
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.core.command.CommandContext
import wiki.ecohub.game.sound.PlayableSound
import wiki.ecohub.ui.text.formatEco

private val enableArgs = setOf("silent", "true", "yes", "1")
private val disableArgs = setOf("false", "no", "0")
private val booleanCompletions = listOf("true", "false")
private val silentCompletions = listOf("silent")

private const val SILENT_ARGS = 4

/**
 * `setpremium <player> <pass> [true|false] [silent]`; `silent` alone means true, without the
 * broadcast.
 */
internal fun CommandBuilder<CommandSender>.setPremiumCommand() = subcommand(
    name = "setpremium",
    permission = "ecobattlepass.command.setpremium"
) {
    executes {
        replying {
            if (args.isEmpty()) {
                reject(Messages::sendSetPremiumUsage)
            }

            val player = players(0).singleOrNull() ?: fail("player-required")
            val pass = battlePass(1)
            val state = args.getOrNull(2)?.lowercase()

            val setToPremium = when {
                state == null || state in enableArgs -> true
                state in disableArgs -> false
                else -> reject(Messages::sendSetPremiumUsage)
            }

            val silent = state == "silent" || args.getOrNull(SILENT_ARGS - 1)?.lowercase() == "silent"

            player.runOwned { applyPremium(player, pass, setToPremium, silent) }
        }
    }

    completes {
        when (args.size) {
            1 -> playerNames()
            2 -> passIds()
            SILENT_ARGS - 1 -> booleanCompletions
            SILENT_ARGS -> silentCompletions
            else -> emptyList()
        }
    }
}

@Suppress("DEPRECATION")
private fun CommandContext<CommandSender>.applyPremium(
    player: Player,
    pass: BattlePass,
    setToPremium: Boolean,
    silent: Boolean
) {
    if (setToPremium == player.hasPremium(pass)) {
        val message = if (setToPremium) Messages.getAlreadyPremium() else Messages.getNotPremium()
        sender.sendMessage(message.replacePlaceholders(player, 0, pass))
        return
    }

    player.setPremium(pass, setToPremium)

    if (setToPremium) {
        PlayableSound.create(plugin.configYml.getSubsection("sound.premium-unlocked"))?.playTo(player)
    }

    val adminMessage = if (setToPremium) Messages.getPremiumGiven() else Messages.getPremiumRemoved()
    sender.sendMessage(adminMessage.replacePlaceholders(player, 0, pass))

    val playerMessage = if (setToPremium) Messages.getPremiumUnlocked() else Messages.getPremiumRevoked()
    player.sendMessage(playerMessage.replacePlaceholders(player, 0, pass))

    if (setToPremium && !silent) {
        Bukkit.broadcastMessage(
            Messages.getPremiumBroadcast()
                .replacePlaceholders(player, 0, pass)
                .formatEco(player)
        )
    }
}
