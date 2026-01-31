package org.ReDiego0.bastionCore.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerExpChangeEvent

class UltimateListener : Listener {

    @EventHandler
    fun onExpChange(event: PlayerExpChangeEvent) {
        event.amount = 0
    }
}