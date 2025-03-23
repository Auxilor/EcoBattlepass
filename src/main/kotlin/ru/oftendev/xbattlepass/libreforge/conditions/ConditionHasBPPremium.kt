package ru.oftendev.xbattlepass.libreforge.conditions

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.*
import com.willfp.libreforge.conditions.Condition
import org.bukkit.entity.Player
import ru.oftendev.xbattlepass.api.hasPremium

object ConditionHasBPPremium: Condition<NoCompileData>("has_premium_battlepass") {
    override fun isMet(
        dispatcher: Dispatcher<*>,
        config: Config,
        holder: ProvidedHolder,
        compileData: NoCompileData
    ): Boolean {
        val player = dispatcher.get<Player>() ?: return false

        return player.hasPremium
    }
}