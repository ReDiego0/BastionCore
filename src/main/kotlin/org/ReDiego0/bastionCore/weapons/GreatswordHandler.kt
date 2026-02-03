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

class GreatswordHandler(private val plugin: BastionCore) {

    fun handleRightClick(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY, 3.0)

        player.playSound(player.location, Sound.ITEM_SHIELD_BLOCK, 1f, 0.5f)
        player.world.spawnParticle(Particle.CLOUD, player.location.add(0.0, 1.0, 0.0), 10, 0.5, 0.5, 0.5, 0.1)
        val dir = player.location.direction.normalize().multiply(1.5)
        player.velocity = Vector(dir.x, 0.2, dir.z)
        player.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, 20, 2))
    }

    fun handlePrimary(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 12.0)
        org.ReDiego0.bastionCore.listener.StaminaListener.changeStamina(player, -6)

        player.playSound(player.location, Sound.BLOCK_ANVIL_USE, 1f, 0.5f)
        player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 40, 10))

        object : BukkitRunnable() {
            var ticks = 0
            override fun run() {
                ticks++
                if (ticks < 30) {
                    player.world.spawnParticle(Particle.FLAME, player.location.add(0.0, 2.0, 0.0), 2, 0.5, 0.5, 0.5, 0.0)
                } else {
                    player.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f)
                    player.world.spawnParticle(Particle.SWEEP_ATTACK, player.location.add(0.0, 1.0, 0.0), 10, 1.0, 1.0, 1.0)

                    val damageLoc = player.location.add(player.location.direction.multiply(2))
                    for (e in player.world.getNearbyEntities(damageLoc, 3.0, 3.0, 3.0)) {
                        if (e is LivingEntity && e != player) {
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