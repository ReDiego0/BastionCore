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
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class KamaHandler(private val plugin: BastionCore) {

    fun handleRightClick(player: Player) {
        if (plugin.cooldownManager.isOnCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY)) return
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY, 12.0)

        player.world.playSound(player.location, Sound.ENTITY_WITHER_SHOOT, 0.5f, 0.5f)
        player.world.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.5f)

        player.world.spawnParticle(Particle.SQUID_INK, player.location.add(0.0, 1.0, 0.0), 30, 0.5, 1.0, 0.5, 0.1)
        player.world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, player.location, 20, 0.2, 0.2, 0.2, 0.05)

        player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 60, 0, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 60, 2, false, false))

        player.sendMessage("§8👻 Desvanecido en las sombras...")
    }

    fun handlePrimary(player: Player) {
        if (plugin.cooldownManager.isOnCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY)) return
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 10.0)

        player.world.playSound(player.location, Sound.ENTITY_PHANTOM_BITE, 1f, 0.8f)
        player.world.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.5f)

        val radius = 5.0
        val damage = 20.0

        object : BukkitRunnable() {
            var angle = 0.0

            override fun run() {
                if (angle >= 360.0) {
                    this.cancel()
                    return
                }

                for (i in 0..1) {
                    val currentAngle = angle + (i * 180)
                    val rad = Math.toRadians(currentAngle)
                    val x = cos(rad) * radius
                    val z = sin(rad) * radius
                    val loc = player.location.add(x, 1.0, z)

                    player.world.spawnParticle(Particle.SQUID_INK, loc, 1, 0.0, 0.0, 0.0, 0.0)
                    player.world.spawnParticle(Particle.SWEEP_ATTACK, loc, 1, 0.0, 0.0, 0.0, 0.0)
                }
                angle += 45.0
            }
        }.runTaskTimer(plugin, 0L, 1L)

        var hits = 0
        for (e in player.world.getNearbyEntities(player.location, radius, 2.0, radius)) {
            if (e is LivingEntity && e != player) {
                if (e.uniqueId == player.uniqueId) continue

                e.damage(damage, player)
                hits++

                player.world.spawnParticle(Particle.CRIT, e.location.add(0.0, 1.5, 0.0), 10)
                player.world.playSound(e.location, Sound.ENTITY_IRON_GOLEM_DAMAGE, 0.5f, 2.0f)

                val pull = player.location.toVector().subtract(e.location.toVector()).normalize().multiply(0.3)
                e.velocity = e.velocity.add(pull)
            }
        }

        if (hits > 0) {
            val duration = 40 + (hits * 20)
            val cappedDuration = min(duration, 200)
            player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, cappedDuration, 1))
            player.playSound(player.location, Sound.ENTITY_WITCH_DRINK, 0.8f, 1.2f)
            player.sendMessage("§c❤ Cosecha de Almas: Regen II x${cappedDuration/20}s ($hits golpes)")
        } else {
            player.sendMessage("§c[!] Fallaste el corte.")
        }
    }
}