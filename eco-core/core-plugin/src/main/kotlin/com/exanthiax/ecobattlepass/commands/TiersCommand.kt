package com.exanthiax.ecobattlepass.commands

import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.exanthiax.ecobattlepass.commands.helpers.Messages
import com.exanthiax.ecobattlepass.gui.BattleTiersGUI
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder

/**
 * An unknown pass sends `pass-not-found` and then the usage, as before.
 */
internal fun CommandBuilder<CommandSender>.tiersCommand() = playerSubcommand(
    name = "tiers",
    permission = "ecobattlepass.command.tier"
) {
    executes {
        replying {
            val passId = args.firstOrNull() ?: reject(Messages::sendTiersUsage)
            val pass = BattlePasses.getByID(passId) ?: reject { target ->
                target.sendMessage(plugin.langYml.getMessage("pass-not-found"))
                Messages.sendTiersUsage(target)
            }

            BattleTiersGUI.createAndOpen(sender, pass)
        }
    }

    completes {
        if (args.size == 1) passIds() else emptyList()
    }
}
