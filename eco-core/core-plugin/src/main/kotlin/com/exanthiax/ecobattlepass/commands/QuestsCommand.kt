package com.exanthiax.ecobattlepass.commands

import com.exanthiax.ecobattlepass.battlepass.BattlePasses
import com.exanthiax.ecobattlepass.commands.helpers.Messages
import com.exanthiax.ecobattlepass.gui.CategoriesGUI
import com.exanthiax.ecobattlepass.gui.QuestsGUI
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder

internal fun CommandBuilder<CommandSender>.questsCommand() = playerSubcommand(
    name = "quests",
    permission = "ecobattlepass.command.quests"
) {
    executes {
        replying {
            if (args.isEmpty()) {
                reject(Messages::sendQuestsUsage)
            }

            val pass = battlePass(0)
            val categoryId = args.getOrNull(1)

            if (categoryId == null) {
                CategoriesGUI(sender, pass).open()
            } else {
                val category = pass.categories.firstOrNull { it.id.equals(categoryId, ignoreCase = true) }
                    ?: fail("invalid-category")

                QuestsGUI(sender, category, wasBack = false).open()
            }
        }
    }

    completes {
        when (args.size) {
            1 -> passIds()
            2 -> BattlePasses.getByID(args[0])?.run { categories.map { it.id } }.orEmpty()
            else -> emptyList()
        }
    }
}
