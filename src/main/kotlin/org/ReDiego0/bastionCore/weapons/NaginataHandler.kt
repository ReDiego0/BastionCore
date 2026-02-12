package org.ReDiego0.bastionCore.combat.weapons

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.manager.CooldownManager
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

class NaginataHandler(private val plugin: BastionCore) {

    fun handleRightClick(player: Player) {
        if (plugin.cooldownManager.isOnCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY)) return
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY, 10.0)

        player.sendMessage("§a🌀 Vórtice Defensivo")
        player.playSound(player.location, Sound.ITEM_TRIDENT_RIPTIDE_1, 1f, 2f)

        object : BukkitRunnable() {
            var t = 0
            val duration = 60

            override fun run() {
                if (!player.isOnline || t >= duration) {
                    this.cancel()
                    return
                }

                val radius = 2.5
                for (i in 0 until 3) {
                    val angle = ((t * 20 + i * 120) % 360).toDouble()
                    val rad = Math.toRadians(angle)
                    val x = cos(rad) * radius
                    val z = sin(rad) * radius

                    player.world.spawnParticle(Particle.SWEEP_ATTACK, player.location.add(x, 1.0, z), 0, 0.0, 0.0, 0.0)
                    player.world.spawnParticle(Particle.CLOUD, player.location.add(x, 1.0, z), 0, 0.0, 0.0, 0.0)
                }

                if (t % 5 == 0) {
                    player.world.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5f, 2f)
                }

                for (e in player.world.getNearbyEntities(player.location, radius, 2.0, radius)) {
                    if (e is LivingEntity && e != player) {
                        val dir = e.location.toVector().subtract(player.location.toVector()).normalize()
                        e.velocity = dir.multiply(0.8).setY(0.2)
                        e.damage(1.0)
                    }

                    if (e is Projectile && e.shooter != player) {
                        e.velocity = e.velocity.multiply(-0.5)
                        player.world.spawnParticle(Particle.CRIT, e.location, 5)
                        player.world.playSound(e.location, Sound.ITEM_SHIELD_BLOCK, 1f, 2f)
                    }
                }

                t += 2
            }
        }.runTaskTimer(plugin, 0L, 2L)
    }

    fun handlePrimary(player: Player) {
        if (plugin.cooldownManager.isOnCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY)) return
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 8.0)

        player.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.5f)
        player.playSound(player.location, Sound.ENTITY_EVOKER_CAST_SPELL, 1f, 2f)

        val direction = player.location.direction.normalize().setY(0)
        val startLoc = player.location.add(0.0, 1.0, 0.0)
        val right = direction.clone().crossProduct(Vector(0, 1, 0)).normalize()

        object : BukkitRunnable() {
            var dist = 0.0
            val maxDist = 10.0
            val width = 4.0

            override fun run() {
                if (dist > maxDist) {
                    this.cancel()
                    return
                }
                dist += 1.0

                val center = startLoc.clone().add(direction.clone().multiply(dist))
                var i = -width
                while (i <= width) {
                    val curve = (i * i) * 0.1

                    val pLoc = center.clone()
                        .add(right.clone().multiply(i))
                        .subtract(direction.clone().multiply(curve))

                    player.world.spawnParticle(Particle.SWEEP_ATTACK, pLoc, 0, direction.x, direction.y, direction.z)
                    player.world.spawnParticle(Particle.END_ROD, pLoc, 0, 0.0, 0.0, 0.0)

                    for (e in player.world.getNearbyEntities(pLoc, 1.0, 1.0, 1.0)) {
                        if (e is LivingEntity && e != player) {
                            if (e.noDamageTicks == 0) {
                                e.damage(30.0, player)
                                e.noDamageTicks = 10
                                player.world.spawnParticle(Particle.CRIT, e.location.add(0.0,1.0,0.0), 5)
                                player.playSound(e.location, Sound.ENTITY_PHANTOM_BITE, 1f, 1.5f)
                            }
                        }
                    }

                    i += 0.5
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }
}