package org.ReDiego0.bastionCore.combat.weapons

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.manager.CooldownManager
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

class KatanaHandler(private val plugin: BastionCore) {

    fun handleRightClick(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY, 8.0)

        val back = player.location.direction.multiply(-1).normalize().multiply(1.0).setY(0.2)
        player.velocity = back
        player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.5f)

        plugin.combatManager.setParry(player.uniqueId, 15)
        player.sendMessage("§bPostura de Desvío")
    }

    fun handlePrimary(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 8.0)

        player.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.5f)
        player.playSound(player.location, Sound.ENTITY_WITHER_SHOOT, 0.5f, 2.0f) // Sonido de "Woosh" agudo

        val startLoc = player.eyeLocation.subtract(0.0, 0.5, 0.0) // Altura de la cintura
        val direction = player.location.direction.normalize()

        val rightVec = direction.clone().crossProduct(Vector(0, 1, 0)).normalize()

        object : BukkitRunnable() {
            var distance = 0.0
            val maxRange = 10.0
            val width = 2.5

            override fun run() {
                if (!player.isOnline || distance > maxRange) {
                    this.cancel()
                    return
                }

                distance += 1.5
                val currentCenter = startLoc.clone().add(direction.clone().multiply(distance))

                if (currentCenter.block.type.isSolid) {
                    player.world.spawnParticle(Particle.EXPLOSION, currentCenter, 1)
                    this.cancel()
                    return
                }

                var i = -width
                while (i <= width) {
                    val particleLoc = currentCenter.clone().add(rightVec.clone().multiply(i))

                    player.world.spawnParticle(Particle.SWEEP_ATTACK, particleLoc, 0, direction.x, direction.y, direction.z)
                    player.world.spawnParticle(Particle.CLOUD, particleLoc, 0, direction.x, direction.y, direction.z, 0.1)

                    i += 0.5
                }

                for (e in player.world.getNearbyEntities(currentCenter, width, 1.5, width)) {
                    if (e is LivingEntity && e != player) {
                        e.noDamageTicks = 0
                        e.damage(30.0, player)

                        e.velocity = direction.clone().multiply(0.8).setY(0.2)
                        player.world.spawnParticle(Particle.CRIT, e.location.add(0.0, 1.0, 0.0), 5)
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }

    fun triggerParryCounter(player: Player) {
        player.world.strikeLightningEffect(player.location)
        player.playSound(player.location, Sound.BLOCK_ANVIL_LAND, 1f, 2f)
        player.sendMessage("§b¡Contraataque Perfecto!")

        val target = player.location.add(player.location.direction.multiply(3))
        player.teleport(target.setDirection(player.location.direction.multiply(-1)))

        val nearby = player.world.getNearbyEntities(player.location, 3.0, 3.0, 3.0)
        for(e in nearby) {
            if(e is LivingEntity && e != player) e.damage(40.0, player)
        }
        plugin.combatManager.removeParry(player.uniqueId)
    }
}