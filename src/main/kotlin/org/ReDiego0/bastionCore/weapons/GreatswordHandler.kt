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

class GreatswordHandler(private val plugin: BastionCore) {

    fun handleRightClick(player: Player) {
        if (plugin.cooldownManager.checkAndNotify(player, CooldownManager.CooldownType.WEAPON_SECONDARY)) return

        player.playSound(player.location, Sound.ITEM_SHIELD_BLOCK, 1f, 0.5f)
        player.playSound(player.location, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.5f, 0.5f) // Golpe seco

        player.world.spawnParticle(Particle.CLOUD, player.location.add(player.location.direction), 15, 0.5, 0.5, 0.5, 0.1)

        val direction = player.location.direction.normalize().multiply(1.5)
        player.velocity = Vector(direction.x, 0.2, direction.z)

        player.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, 5, 2))

        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY, 3.0)
    }

    fun handlePrimary(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 12.0)

        // Sonido de carga progresiva
        player.playSound(player.location, Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 2f)
        player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 40, 10))

        object : BukkitRunnable() {
            var ticks = 0
            override fun run() {
                ticks++
                if (ticks < 30) {
                    player.world.spawnParticle(Particle.FLAME, player.location.add(0.0, 2.5, 0.0), 3, 0.2, 0.2, 0.2, 0.05)
                    if (ticks % 10 == 0) player.playSound(player.location, Sound.BLOCK_ANVIL_LAND, 0.5f + (ticks/60f), 0.5f)
                } else {
                    player.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.8f)
                    player.playSound(player.location, Sound.ENTITY_IRON_GOLEM_DEATH, 1f, 0.5f)

                    val start = player.location
                    val dir = player.location.direction.setY(0).normalize()

                    for (i in 1..4) {
                        val loc = start.clone().add(dir.clone().multiply(i.toDouble()))
                        player.world.spawnParticle(Particle.BLOCK, loc, 15, 0.5, 0.5, 0.5, Material.COBBLESTONE.createBlockData())
                        player.world.spawnParticle(Particle.EXPLOSION, loc, 1)
                    }

                    val damageLoc = player.location.add(player.location.direction.multiply(2))
                    for (e in player.world.getNearbyEntities(damageLoc, 3.5, 3.5, 3.5)) {
                        if (e is LivingEntity && e != player) {
                            e.noDamageTicks = 0
                            e.damage(50.0, player)
                            e.velocity = Vector(0, 1, 0)
                        }
                    }
                    this.cancel()
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }
}