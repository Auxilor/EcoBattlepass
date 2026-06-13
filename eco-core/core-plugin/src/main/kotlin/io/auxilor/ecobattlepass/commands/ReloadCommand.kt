package io.auxilor.ecobattlepass.commands

import io.auxilor.ecobattlepass.battlepass.BattlePasses
import io.auxilor.ecobattlepass.categories.Categories
import io.auxilor.ecobattlepass.plugin
import io.auxilor.ecobattlepass.quests.BattleQuests
import io.auxilor.ecobattlepass.rewards.Rewards
import io.auxilor.ecobattlepass.tasks.BattleTasks
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.util.NumberUtils
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.toNiceString
import org.bukkit.command.CommandSender

object ReloadCommand: PluginCommand(
    plugin,
    "reload",
    "ecobattlepass.command.reload",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender.sendMessage(
            plugin.langYml.getMessage("reloaded", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                .replace("%time%", plugin.reloadWithTime().toNiceString())
                .replace("%count%", BattlePasses.values().size.toString())
        )
    }
}