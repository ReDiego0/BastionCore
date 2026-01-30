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
            player.saturation = 0f
            player.setMetadata(STAMINA_CHANGE_KEY, FixedMetadataValue(BastionCore.instance, true))

            val oldLevel = player.foodLevel
            val newLevel = (oldLevel + amount).coerceIn(0, 20)
            if (amount < 0) {
                System.out.println("[DEBUG] Bajando estamina a ${player.name}: $oldLevel -> $newLevel")
            }
            player.foodLevel = newLevel
            player.removeMetadata(STAMINA_CHANGE_KEY, BastionCore.instance)
        }
    }

    @EventHandler
    fun onFoodChange(event: FoodLevelChangeEvent) {
        if (event.entity !is Player) return
        val player = event.entity as Player
        event.entity.saturation = 0f

        if (player.hasMetadata(STAMINA_CHANGE_KEY)) {
            event.isCancelled = false
        } else {
            event.isCancelled = true
        }
    }
}