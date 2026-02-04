package org.ReDiego0.bastionCore.task

import org.ReDiego0.bastionCore.BastionCore
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.scheduler.BukkitRunnable

class CitadelTask(private val plugin: BastionCore) : BukkitRunnable() {

    override fun run() {
        val citadel = Bukkit.getWorld(plugin.citadelWorldName) ?: return

        for (player in citadel.players) {
            if (player.isFlying && player.gameMode != org.bukkit.GameMode.SPECTATOR) {
                val loc = player.location

                player.world.spawnParticle(Particle.CLOUD, loc, 0, 0.0, 0.0, 0.0, 0.05)
            }
        }
    }
}