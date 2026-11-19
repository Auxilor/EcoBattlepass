package com.exanthiax.ecobattlepass.commands

import com.exanthiax.ecobattlepass.EcoBattlePass
import com.exanthiax.ecobattlepass.commands.give.giveTaskXPCommand
import com.exanthiax.ecobattlepass.commands.give.giveTiersCommand
import com.exanthiax.ecobattlepass.commands.give.giveXPCommand
import com.exanthiax.ecobattlepass.commands.helpers.Messages
import com.exanthiax.ecobattlepass.commands.reset.resetBattlepassCommand
import com.exanthiax.ecobattlepass.commands.reset.resetTaskCommand
import wiki.ecohub.core.command.command

internal fun EcoBattlePass.ecoBattlePassCommand() = command(
    name = "ecobattlepass",
    permission = "ecobattlepass.command.ecobattlepass"
) {
    questsCommand()
    reloadCommand()
    completeTaskCommand()

    subcommand(name = "reset", permission = "ecobattlepass.command.reset") {
        resetBattlepassCommand()
        resetTaskCommand()

        executes { Messages.sendResetUsage(sender) }
    }

    subcommand(name = "give", permission = "ecobattlepass.command.give") {
        giveXPCommand()
        giveTiersCommand()
        giveTaskXPCommand()

        executes { Messages.sendGiveUsage(sender) }
    }

    tiersCommand()
    setPremiumCommand()
}
