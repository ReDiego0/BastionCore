package org.ReDiego0.bastionCore.listener

import org.ReDiego0.bastionCore.BastionCore
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class CitadelListener(private val plugin: BastionCore) : Listener {

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        if (event.entity !is Player) return
        val player = event.entity as Player

        if (event.cause == EntityDamageEvent.DamageCause.FALL) {
            if (player.world.name == plugin.citadelWorldName) {
                event.isCancelled = true
            }
        }
    }
}