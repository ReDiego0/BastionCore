package org.ReDiego0.bastionCore.combat.weapons

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.manager.CooldownManager
import org.bukkit.Color
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

        object : BukkitRunnable() {
            var angle = 0.0
            val radius = 3.5

            override fun run() {
                if (angle >= 360.0) {
                    this.cancel()
                    return
                }

                for (i in 0..60 step 10) {
                    val rad = Math.toRadians(angle + i)
                    val x = cos(rad) * radius
                    val z = sin(rad) * radius

                    val loc = player.location.add(x, 1.0, z)

                    val dustOptions = Particle.DustOptions(Color.RED, 1.0f)
                    player.world.spawnParticle(Particle.DUST, loc, 1, 0.0, 0.0, 0.0, 0.0, dustOptions)

                    player.world.spawnParticle(Particle.SQUID_INK, loc, 0, 0.0, 0.0, 0.0)
                }

                angle += 60.0
            }
        }.runTaskTimer(plugin, 0L, 1L)

        var totalHeal = 0.0
        val maxHeal = 10.0
        val damage = 35.0

        for (e in player.world.getNearbyEntities(player.location, 3.5, 2.0, 3.5)) {
            if (e is LivingEntity && e != player) {
                e.damage(damage, player)

                val dirToPlayer = player.location.toVector().subtract(e.location.toVector()).normalize()
                player.world.spawnParticle(Particle.HEART, e.location.add(0.0, 1.5, 0.0), 1, dirToPlayer.x, dirToPlayer.y, dirToPlayer.z, 0.5)

                player.playSound(e.location, Sound.ENTITY_WITCH_DRINK, 1f, 1.5f)

                totalHeal += 2.0
            }
        }

        if (totalHeal > 0) {
            val finalHeal = min(totalHeal, maxHeal)
            val newHealth = min(player.health + finalHeal, player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)?.value ?: 20.0)
            player.health = newHealth

            player.sendMessage("§c❤ Cosechaste ${finalHeal.toInt()} HP")
        }
    }
}