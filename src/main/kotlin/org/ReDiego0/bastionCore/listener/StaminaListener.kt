package org.ReDiego0.bastionCore.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.FoodLevelChangeEvent

class StaminaListener : Listener {

    @EventHandler
    fun onFoodChange(event: FoodLevelChangeEvent) {
        event.isCancelled = true
    }
}