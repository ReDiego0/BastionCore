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
import kotlin.random.Random

class TekkoHandler(private val plugin: BastionCore) {

    fun handleRightClick(player: Player) {
        if (plugin.cooldownManager.isOnCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY)) return
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY, 12.0)

        player.world.playSound(player.location, Sound.ENTITY_BLAZE_SHOOT, 1f, 0.5f)
        player.world.playSound(player.location, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 0.5f, 1.5f)
        player.world.spawnParticle(Particle.CLOUD, player.location.add(0.0, 1.0, 0.0), 20, 0.3, 0.5, 0.3, 0.1)
        player.world.spawnParticle(Particle.FLAME, player.location, 10, 0.3, 0.5, 0.3, 0.05)

        val negativeEffects = listOf(
            PotionEffectType.SLOWNESS, PotionEffectType.WITHER,
            PotionEffectType.POISON, PotionEffectType.WEAKNESS, PotionEffectType.BLINDNESS
        )
        for (effect in negativeEffects) {
            player.removePotionEffect(effect)
        }

        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 80, 2))
        player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 40, 2))

        player.sendMessage("§c♨ ¡Límites rotos! Cuerpo purificado.")
    }

    fun handlePrimary(player: Player) {
        if (plugin.cooldownManager.isOnCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY)) return
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 10.0)

        player.sendMessage("§e👊 ¡RÁFAGA ASURA!")
        player.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 2f)

        player.velocity = player.location.direction.multiply(0.5).setY(0.2)

        object : BukkitRunnable() {
            var punches = 0
            val maxPunches = 20

            override fun run() {
                if (!player.isOnline || player.isDead) {
                    this.cancel()
                    return
                }

                if (punches >= maxPunches) {
                    this.cancel()
                    performFinisher(player)
                    return
                }

                val eyeLoc = player.eyeLocation
                val direction = eyeLoc.direction.normalize()

                val rX = (Random.nextDouble() - 0.5) * 1.5
                val rY = (Random.nextDouble() - 0.5) * 1.5
                val rZ = (Random.nextDouble() - 0.5) * 1.5

                val targetLoc = eyeLoc.clone().add(direction.multiply(2.5)).add(rX, rY, rZ)

                player.world.spawnParticle(Particle.EXPLOSION, targetLoc, 1, 0.0, 0.0, 0.0, 0.0)
                player.world.spawnParticle(Particle.SWEEP_ATTACK, targetLoc, 0, direction.x, direction.y, direction.z)

                if (punches % 2 == 0) {
                    player.world.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 0.8f, 1.5f + (Random.nextFloat() * 0.5f))
                }

                for (e in player.world.getNearbyEntities(player.location.add(direction.multiply(2)), 2.5, 2.5, 2.5)) {
                    if (e is LivingEntity && e != player) {
                        e.noDamageTicks = 0
                        e.damage(3.0, player)
                        e.velocity = Vector(0,0,0)
                    }
                }

                punches++
            }
        }.runTaskTimer(plugin, 0L, 2L)
    }

    private fun performFinisher(player: Player) {
        val direction = player.location.direction.normalize()
        val loc = player.location.add(direction.multiply(2))

        player.world.createExplosion(loc, 0F, false)
        player.world.spawnParticle(Particle.FLASH, loc, 2)
        player.world.spawnParticle(Particle.LAVA, loc, 10, 0.5, 0.5, 0.5, 0.2)

        player.world.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f)
        player.world.playSound(player.location, Sound.ENTITY_IRON_GOLEM_DEATH, 1f, 2f)

        for (e in player.world.getNearbyEntities(loc, 3.0, 3.0, 3.0)) {
            if (e is LivingEntity && e != player) {
                e.damage(15.0, player)
                e.velocity = direction.multiply(2.5).setY(0.6)
            }
        }
    }
}