package org.ReDiego0.bastionCore.combat.weapons

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.manager.CooldownManager
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class DualBladesHandler(private val plugin: BastionCore) {

    fun handleRightClick(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY, 2.0)

        val dash = player.location.direction.normalize().multiply(1.5)
        player.velocity = dash.setY(0.1)

        player.world.spawnParticle(Particle.SMOKE, player.location, 5, 0.2, 0.2, 0.2, 0.0)
        player.playSound(player.location, Sound.ENTITY_PHANTOM_FLAP, 1f, 2f)
    }

    fun handlePrimary(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 10.0)
        val loc = player.location

        object : BukkitRunnable() {
            var count = 0
            override fun run() {
                if (count >= 15) {
                    this.cancel()
                    return
                }
                count++

                player.teleport(loc)

                player.world.spawnParticle(Particle.SWEEP_ATTACK, loc.add(loc.direction).add(0.0, 1.0, 0.0), 1)
                player.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 2f)

                val front = loc.add(loc.direction.multiply(1.5))
                for (e in player.world.getNearbyEntities(front, 1.5, 1.5, 1.5)) {
                    if (e is LivingEntity && e != player) {
                        e.noDamageTicks = 0
                        e.damage(3.0, player)
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }
}