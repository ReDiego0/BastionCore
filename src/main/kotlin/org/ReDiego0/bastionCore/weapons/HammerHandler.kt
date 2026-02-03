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

class HammerHandler(private val plugin: BastionCore) {

    fun handleRightClick(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY, 8.0)

        player.velocity = player.location.direction.multiply(1.0).setY(1.2)
        player.playSound(player.location, Sound.ENTITY_IRON_GOLEM_ATTACK, 1f, 0.5f)

        for (e in player.world.getNearbyEntities(player.location, 2.0, 2.0, 2.0)) {
            if (e is LivingEntity && e != player) {
                e.damage(10.0, player)
                e.velocity = Vector(0, 1, 0)
            }
        }

        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            if (!player.isOnline) return@Runnable
            if (!player.isOnline) return@Runnable

            try {
                val blockData = Material.DIRT.createBlockData()
                player.world.spawnParticle(Particle.BLOCK, player.location, 50, 1.0, 0.1, 1.0, 0.1, blockData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            player.playSound(player.location, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1f, 0.5f)
            val impactRadius = 5.0
            for (e in player.world.getNearbyEntities(player.location, impactRadius, 3.0, impactRadius)) {
                if (e is LivingEntity && e != player) {
                    e.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 60, 2))
                    e.noDamageTicks = 0
                    e.damage(15.0, player)
                    e.velocity = Vector(0.0, 0.5, 0.0)
                }
            }
        }, 15L)
    }

    fun handlePrimary(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 15.0)
        object : BukkitRunnable() {
            var spins = 0
            override fun run() {
                if (!player.isOnline || spins >= 30) {
                    this.cancel()
                    return
                }
                spins++

                player.world.spawnParticle(Particle.SWEEP_ATTACK, player.location.add(0.0, 1.0, 0.0), 1)
                player.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1.5f)

                for (e in player.world.getNearbyEntities(player.location, 2.5, 2.0, 2.5)) {
                    if (e is LivingEntity && e != player) {
                        e.damage(4.0, player)
                        val dir = e.location.toVector().subtract(player.location.toVector()).normalize().multiply(0.5)
                        e.velocity = dir
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L)
    }
}