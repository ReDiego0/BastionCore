package org.ReDiego0.bastionCore.combat.weapons

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.manager.CooldownManager
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

class DualBladesHandler(private val plugin: BastionCore) {

    fun handleRightClick(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY, 2.0)

        val dash = player.location.direction.normalize().multiply(2.5)
        player.velocity = dash.setY(0.2)

        player.world.spawnParticle(Particle.SMOKE, player.location, 5, 0.2, 0.2, 0.2, 0.0)
        player.playSound(player.location, Sound.ENTITY_PHANTOM_FLAP, 1f, 2f)
    }

    fun handlePrimary(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 10.0)

        val anchorLoc = player.location.clone()

        object : BukkitRunnable() {
            var count = 0
            override fun run() {
                if (count >= 15 || !player.isOnline) {
                    this.cancel()
                    return
                }
                count++

                val currentLook = player.location
                anchorLoc.yaw = currentLook.yaw
                anchorLoc.pitch = currentLook.pitch
                player.teleport(anchorLoc)
                player.velocity = Vector(0, 0, 0)

                player.world.spawnParticle(Particle.SWEEP_ATTACK, anchorLoc.add(anchorLoc.direction).add(0.0, 1.0, 0.0), 1)
                player.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 2f)

                val front = anchorLoc.clone().add(anchorLoc.direction.multiply(1.5))
                for (e in player.world.getNearbyEntities(front, 2.0, 2.0, 2.0)) {
                    if (e is LivingEntity && e != player) {
                        e.noDamageTicks = 0
                        e.damage(3.0, player)
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }
}