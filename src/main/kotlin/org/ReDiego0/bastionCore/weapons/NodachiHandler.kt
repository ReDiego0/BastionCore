package org.ReDiego0.bastionCore.combat.weapons

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.manager.CooldownManager
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

class NodachiHandler(private val plugin: BastionCore) {

    fun handleRightClick(player: Player) {
        if (plugin.cooldownManager.isOnCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY)) return
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY, 15.0)

        plugin.combatManager.setBlocking(player.uniqueId, 60)

        player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 60, 4, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, 60, 2))

        player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 1f, 0.5f)
        player.playSound(player.location, Sound.ENTITY_IRON_GOLEM_HURT, 1f, 0.1f)

        player.sendMessage("§8🛡 Guardia del Titán (3s)")

        object : BukkitRunnable() {
            var t = 0
            override fun run() {
                if (!plugin.combatManager.isBlocking(player.uniqueId) || t >= 60) {
                    this.cancel()
                    return
                }

                val loc = player.location.add(0.0, 1.0, 0.0)
                player.world.spawnParticle(Particle.FALLING_OBSIDIAN_TEAR, loc, 2, 0.5, 1.0, 0.5, 0.1)
                player.world.spawnParticle(Particle.SMOKE, loc, 1, 0.3, 0.5, 0.3, 0.0)

                t += 5
            }
        }.runTaskTimer(plugin, 0L, 5L)
    }

    fun handlePrimary(player: Player) {
        if (plugin.cooldownManager.isOnCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY)) return
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 18.0)

        player.sendMessage("§c⚠ Cargando golpe sísmico...")

        player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 35, 10, false, false))
        player.playSound(player.location, Sound.BLOCK_DEEPSLATE_BREAK, 1f, 0.5f)

        object : BukkitRunnable() {
            var charge = 0
            val maxCharge = 30

            override fun run() {
                if (!player.isOnline) {
                    this.cancel()
                    return
                }

                val loc = player.location
                player.world.spawnParticle(Particle.BLOCK_CRUMBLE, loc.add(0.0, 0.5, 0.0), 5, 1.0, 0.0, 1.0, 0.1, Material.DIRT.createBlockData())

                charge += 5
                if (charge >= maxCharge) {
                    this.cancel()
                    smashGround(player)
                }
            }
        }.runTaskTimer(plugin, 0L, 5L)
    }

    private fun smashGround(player: Player) {
        player.playSound(player.location, Sound.ENTITY_WARDEN_DIG, 1f, 0.5f) // Sonido ultra bajo y fuerte
        player.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f)

        val start = player.location.add(0.0, 0.5, 0.0)
        val dir = player.location.direction.clone().setY(0).normalize() // Solo horizontal

        object : BukkitRunnable() {
            var dist = 0.0
            val maxDist = 12.0

            override fun run() {
                if (dist > maxDist) {
                    this.cancel()
                    return
                }

                for (i in 0..1) {
                    dist += 1.0
                    if (dist > maxDist) break

                    val point = start.clone().add(dir.clone().multiply(dist))

                    var groundPoint = point
                    if (point.block.type.isAir) {
                        if (point.clone().subtract(0.0, 1.0, 0.0).block.type.isSolid) {
                            groundPoint = point.clone().subtract(0.0, 1.0, 0.0)
                        }
                    }

                    player.world.spawnParticle(Particle.BLOCK_CRUMBLE, groundPoint.clone().add(0.0, 1.0, 0.0), 15, 0.5, 0.5, 0.5, 0.0, Material.COARSE_DIRT.createBlockData())
                    player.world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, groundPoint.clone().add(0.0, 1.0, 0.0), 5, 0.2, 0.2, 0.2, 0.05)
                    player.world.spawnParticle(Particle.EXPLOSION, groundPoint.clone().add(0.0, 0.5, 0.0), 1)

                    for (e in player.world.getNearbyEntities(groundPoint, 2.0, 3.0, 2.0)) {
                        if (e is LivingEntity && e != player) {
                            e.damage(45.0, player)
                            e.velocity = Vector(0.0, 1.2, 0.0).add(dir.clone().multiply(0.5))

                            player.playSound(e.location, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1f, 0.5f)
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }
}