package org.ReDiego0.bastionCore.combat.weapons

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.manager.CooldownManager
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

class DualBladesHandler(private val plugin: BastionCore) {

    fun handleRightClick(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY, 2.0)

        val dash = player.location.direction.normalize().multiply(2.5)
        player.velocity = dash.setY(0.2)

        player.world.spawnParticle(Particle.SQUID_INK, player.location, 10, 0.2, 0.2, 0.2, 0.1)
        player.playSound(player.location, Sound.ENTITY_PHANTOM_FLAP, 1f, 2f)
    }

    fun handlePrimary(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 10.0)

        val anchorLoc = player.location.clone()

        object : BukkitRunnable() {
            var count = 0
            override fun run() {
                if (count >= 15 || !player.isOnline) {
                    this.cancel()
                    return
                }
                count++

                val currentLook = player.location
                anchorLoc.yaw = currentLook.yaw
                anchorLoc.pitch = currentLook.pitch
                player.teleport(anchorLoc)
                player.velocity = Vector(0,0,0)

                val angle = (count * 30) % 360
                val rad = Math.toRadians(angle.toDouble())
                val x = cos(rad) * 1.5
                val z = sin(rad) * 1.5
                val loc = anchorLoc.clone().add(x, 1.0, z)

                val red = Particle.DustOptions(Color.RED, 1f)
                val black = Particle.DustOptions(Color.BLACK, 1f)

                val chosenColor = if(count % 2 == 0) red else black
                player.world.spawnParticle(Particle.DUST, loc, 1, 0.0, 0.0, 0.0, 0.0, chosenColor)

                player.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 2f)

                val front = anchorLoc.clone().add(anchorLoc.direction.multiply(2.0))
                for (e in player.world.getNearbyEntities(front, 2.5, 2.5, 2.5)) {
                    if (e is LivingEntity && e != player) {
                        e.noDamageTicks = 0
                        e.damage(3.0, player)
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }
}