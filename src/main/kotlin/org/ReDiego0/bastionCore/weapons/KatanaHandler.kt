package org.ReDiego0.bastionCore.combat.weapons

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.manager.CooldownManager
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

        player.playSound(player.location, Sound.BLOCK_IRON_DOOR_OPEN, 1f, 2f)
        player.sendMessage("§7Preparando...")

        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            if (!player.isOnline) return@Runnable

            val startLoc = player.location
            val direction = startLoc.direction.normalize().multiply(6)

            val dest = startLoc.clone().add(direction)
            if (dest.block.type.isSolid) {
                player.velocity = direction.normalize().multiply(1)
            } else {
                player.teleport(dest) // Teleport es más seguro para hitboxes que velocity
                player.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.5f)
                player.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 2f)
            }

            val steps = 20
            val stepVec = direction.clone().multiply(1.0/steps)
            val drawLoc = startLoc.clone().add(0.0, 1.0, 0.0)

            for (i in 0..steps) {
                player.world.spawnParticle(Particle.SWEEP_ATTACK, drawLoc, 1)
                player.world.spawnParticle(Particle.CRIT, drawLoc, 5, 0.2, 0.2, 0.2, 0.0)
                drawLoc.add(stepVec)
            }

            val damageBoxCenter = startLoc.clone().add(direction.clone().multiply(0.5))

            for (e in player.world.getNearbyEntities(damageBoxCenter, 5.0, 3.0, 5.0)) {
                if (e is LivingEntity && e != player) {
                    e.noDamageTicks = 0
                    e.damage(35.0, player)
                    player.world.spawnParticle(Particle.BLOCK, e.location.add(0.0, 1.0, 0.0), 10, 0.3, 0.3, 0.3, org.bukkit.Material.REDSTONE_BLOCK.createBlockData())
                }
            }

        }, 10L)
    }

    fun triggerParryCounter(player: Player) {
        player.playSound(player.location, Sound.BLOCK_ANVIL_LAND, 1f, 2f)
        player.sendMessage("§b¡Contraataque Perfecto!")

        val target = player.location.add(player.location.direction.multiply(3))
        player.teleport(target.setDirection(player.location.direction.multiply(-1)))

        val nearby = player.world.getNearbyEntities(player.location, 3.0, 3.0, 3.0)
        for(e in nearby) {
            if(e is LivingEntity && e != player) e.damage(35.0, player)
        }
        plugin.combatManager.removeParry(player.uniqueId)
    }
}