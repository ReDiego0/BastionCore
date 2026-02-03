package org.ReDiego0.bastionCore.combat.weapons

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.manager.CooldownManager
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

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
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 12.0)

        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 0.5f)
        player.world.spawnParticle(Particle.CRIT, player.location, 10, 0.5, 1.0, 0.5) // Aura cargando

        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            if (!player.isOnline) return@Runnable

            val startLoc = player.location.clone()
            val direction = startLoc.direction.normalize()
            val distance = 8.0

            val dest = startLoc.clone().add(direction.clone().multiply(distance))
            if (!dest.block.type.isSolid) player.teleport(dest)
            player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.5f)

            val points = 20
            val step = direction.clone().multiply(distance / points)
            val drawLoc = startLoc.clone().add(0.0, 1.0, 0.0)

            for (i in 0..points) {
                val dust = Particle.DustOptions(Color.AQUA, 1.0f)
                player.world.spawnParticle(Particle.DUST, drawLoc, 1, 0.0, 0.0, 0.0, 0.0, dust)
                drawLoc.add(step)
            }

            plugin.server.scheduler.runTaskLater(plugin, Runnable {
                player.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.5f)
                player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 0.5f, 2f) // "Clang" metálico fuerte

                val burstLoc = startLoc.clone().add(0.0, 1.0, 0.0)
                for (i in 0..points step 2) {
                    player.world.spawnParticle(Particle.SWEEP_ATTACK, burstLoc, 1) // Cortes
                    player.world.spawnParticle(Particle.FLASH, burstLoc, 1) // Destello
                    burstLoc.add(step.clone().multiply(2))
                }

                val center = startLoc.clone().add(direction.clone().multiply(distance / 2))
                for (e in player.world.getNearbyEntities(center, distance/2 + 2, 3.0, distance/2 + 2)) {
                    if (e is LivingEntity && e != player) {
                        e.noDamageTicks = 0
                        e.damage(35.0, player)
                        player.world.spawnParticle(Particle.BLOCK, e.location.add(0.0,1.0,0.0), 15, 0.2, 0.2, 0.2, org.bukkit.Material.REDSTONE_BLOCK.createBlockData())
                    }
                }
            }, 5L)

        }, 8L)
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