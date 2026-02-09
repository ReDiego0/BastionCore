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

class KatanaHandler(private val plugin: BastionCore) {

    fun handleRightClick(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY, 8.0)
        player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.5f)
        player.playSound(player.location, Sound.ITEM_SHIELD_BLOCK, 1f, 0.5f) // Sonido sutil de "preparar bloqueo"
        player.world.spawnParticle(Particle.ENCHANT, player.location, 15, 0.3, 0.1, 0.3, 1.0)
        plugin.combatManager.setParry(player.uniqueId, 35)
        player.sendMessage("§b⚡ Postura de Desvío (Activa)")
    }

    fun handlePrimary(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 12.0)

        player.sendMessage("§3Concentrando corte...")
        player.playSound(player.location, Sound.ITEM_TRIDENT_THUNDER, 1f, 0.5f)

        player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 55, 5, false, false))

        object : BukkitRunnable() {
            var chargeTicks = 0
            val chargeTime = 50

            override fun run() {
                if (!player.isOnline || player.isDead) {
                    this.cancel()
                    return
                }

                if (chargeTicks < chargeTime) {
                    player.world.spawnParticle(Particle.PORTAL, player.location.add(0.0, 1.0, 0.0), 10, 0.5, 1.0, 0.5, 0.1)
                    player.world.spawnParticle(Particle.CRIT, player.location.add(0.0, 1.0, 0.0), 2, 0.2, 0.5, 0.2, 0.0)

                    if (chargeTicks % 10 == 0) {
                        val pitch = 0.5f + (chargeTicks.toFloat() / chargeTime.toFloat())
                        player.playSound(player.location, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.5f, pitch)
                    }

                    chargeTicks++
                } else {
                    this.cancel()
                    fireVoidSlash(player)
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }

    private fun fireVoidSlash(player: Player) {
        player.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.5f)
        player.playSound(player.location, Sound.ENTITY_WARDEN_SONIC_BOOM, 1f, 2.0f)

        val startLoc = player.eyeLocation.subtract(0.0, 0.5, 0.0)
        val direction = player.location.direction.normalize()

        val rightVec = direction.clone().crossProduct(Vector(0, 1, 0)).normalize()

        object : BukkitRunnable() {
            var distance = 0.0
            val maxRange = 25.0
            val width = 5.0

            override fun run() {
                if (!player.isOnline || distance > maxRange) {
                    this.cancel()
                    return
                }

                distance += 1.5
                val currentCenter = startLoc.clone().add(direction.clone().multiply(distance))

                if (currentCenter.block.type.isSolid) {
                    player.world.spawnParticle(Particle.EXPLOSION, currentCenter, 1)
                    player.playSound(currentCenter, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 2f)
                    this.cancel()
                    return
                }

                var i = -width
                while (i <= width) {
                    val particleLoc = currentCenter.clone().add(rightVec.clone().multiply(i))

                    player.world.spawnParticle(Particle.SWEEP_ATTACK, particleLoc, 0, direction.x, direction.y, direction.z)
                    player.world.spawnParticle(Particle.CLOUD, particleLoc, 0, direction.x, direction.y, direction.z, 0.1)

                    if (i % 1.0 == 0.0) {
                        player.world.spawnParticle(Particle.WAX_OFF, particleLoc, 1, 0.0, 0.0, 0.0, 0.0)
                    }

                    i += 0.5
                }

                for (e in player.world.getNearbyEntities(currentCenter, width, 2.0, width)) {
                    if (e is LivingEntity && e != player) {
                        e.noDamageTicks = 0
                        e.damage(60.0, player)

                        e.velocity = direction.clone().multiply(1.5).setY(0.4)
                        player.world.spawnParticle(Particle.CRIT, e.location.add(0.0, 1.0, 0.0), 10)
                        player.playSound(e.location, Sound.ENTITY_IRON_GOLEM_DAMAGE, 1f, 0.5f)
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
        if (!target.block.type.isSolid && !target.clone().add(0.0,1.0,0.0).block.type.isSolid) {
            player.teleport(target.setDirection(player.location.direction.multiply(-1)))
        }

        val nearby = player.world.getNearbyEntities(player.location, 4.0, 4.0, 4.0)
        for(e in nearby) {
            if(e is LivingEntity && e != player) {
                e.damage(40.0, player)
                e.velocity = Vector(0, 1, 0)
            }
        }
        plugin.combatManager.removeParry(player.uniqueId)
    }
}