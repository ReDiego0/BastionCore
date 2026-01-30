package org.ReDiego0.bastionCore.listener

import org.ReDiego0.bastionCore.BastionCore
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.metadata.FixedMetadataValue

class StaminaListener : Listener {

    companion object {
        private const val STAMINA_CHANGE_KEY = "BastionStaminaUpdate"

        fun changeStamina(player: Player, amount: Int) {
            player.setMetadata(STAMINA_CHANGE_KEY, FixedMetadataValue(BastionCore.instance, true))
            val newLevel = (player.foodLevel + amount).coerceIn(0, 20)
            player.foodLevel = newLevel
            player.removeMetadata(STAMINA_CHANGE_KEY, BastionCore.instance)
        }
    }

    @EventHandler
    fun onFoodChange(event: FoodLevelChangeEvent) {
        if (event.entity !is Player) return
        val player = event.entity as Player
        if (player.hasMetadata(STAMINA_CHANGE_KEY)) {
            event.isCancelled = false
        } else {
            event.isCancelled = true
        }
    }
}