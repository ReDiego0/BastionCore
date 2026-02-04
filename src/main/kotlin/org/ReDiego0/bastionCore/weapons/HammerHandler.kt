package org.ReDiego0.bastionCore.combat.weapons

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.manager.CooldownManager
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

class HammerHandler(private val plugin: BastionCore) {

    fun handleRightClick(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY, 10.0)

        player.playSound(player.location, Sound.ENTITY_IRON_GOLEM_ATTACK, 1f, 0.8f)
        player.playSound(player.location, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1f, 1f)

        player.world.spawnParticle(Particle.EXPLOSION, player.location, 3, 0.5, 0.5, 0.5, 0.0)
        player.world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, player.location, 10, 0.2, 0.1, 0.2, 0.1)

        val impactLoc = player.location.add(player.location.direction.multiply(1.5))
        for (e in player.world.getNearbyEntities(impactLoc, 3.5, 3.5, 3.5)) {
            if (e is LivingEntity && e != player) {
                e.noDamageTicks = 0
                e.damage(18.0, player)

                e.velocity = Vector(0.0, 0.6, 0.0)
                e.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 40, 2))
            }
        }

        val launchDir = player.location.direction.multiply(0.5).setY(1.3)
        player.velocity = launchDir
    }

    fun handlePrimary(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 15.0)
        val range = 4.5
        object : BukkitRunnable() {
            var spins = 0
            override fun run() {
                if (!player.isOnline || spins >= 30) {
                    this.cancel()
                    return
                }
                spins++

                if (spins % 3 == 0) {
                    player.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.5f)
                }

                val center = player.location.add(0.0, 1.0, 0.0)

                player.world.spawnParticle(Particle.SWEEP_ATTACK, center, 1)

                val angle = (spins * 20.0) % 360.0
                val rad = Math.toRadians(angle)

                val x1 = cos(rad) * range
                val z1 = sin(rad) * range
                player.world.spawnParticle(Particle.CLOUD, center.clone().add(x1, 0.0, z1), 0, 0.0, 0.0, 0.0, 0.1)
                player.world.spawnParticle(Particle.CRIT, center.clone().add(-x1, 0.0, -z1), 0, 0.0, 0.0, 0.0, 0.1)

                for (e in player.world.getNearbyEntities(player.location, range, 3.0, range)) {
                    if (e is LivingEntity && e != player) {
                        e.damage(5.0, player)
                        val dir = e.location.toVector().subtract(player.location.toVector()).normalize().multiply(0.4).setY(0.1)
                        e.velocity = dir
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }
}