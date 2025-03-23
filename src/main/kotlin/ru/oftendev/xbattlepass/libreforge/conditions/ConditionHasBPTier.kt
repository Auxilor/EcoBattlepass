package ru.oftendev.xbattlepass.libreforge.conditions

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.*
import com.willfp.libreforge.conditions.Condition
import org.bukkit.entity.Player
import ru.oftendev.xbattlepass.api.bpTier

object ConditionHasBPTier: Condition<NoCompileData>("has_battlepass_tier") {
    override val arguments: ConfigArguments = arguments {
        require("tier", "You must specify the tier!")
    }

    override fun isMet(
        dispatcher: Dispatcher<*>,
        config: Config,
        holder: ProvidedHolder,
        compileData: NoCompileData
    ): Boolean {
        val player = dispatcher.get<Player>() ?: return false

        return player.bpTier >= config.getIntFromExpression("tier", player)
    }
}