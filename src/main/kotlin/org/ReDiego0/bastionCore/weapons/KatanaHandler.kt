package org.ReDiego0.bastionCore.combat.weapons

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.manager.CooldownManager
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
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
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 12.0)

        player.velocity = Vector(0.0, 1.5, 0.0)
        player.playSound(player.location, Sound.ENTITY_BAT_TAKEOFF, 1f, 1f)

        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            if (!player.isOnline) return@Runnable

            player.velocity = Vector(0.0, -2.0, 0.0)

            player.world.spawnParticle(Particle.SWEEP_ATTACK, player.location.add(0.0, 1.0, 0.0), 5)
            player.world.spawnParticle(Particle.CRIT, player.location, 20, 0.5, 1.5, 0.5)

            player.playSound(player.location, Sound.ENTITY_IRON_GOLEM_DAMAGE, 1f, 2f)

            for (e in player.world.getNearbyEntities(player.location, 5.0, 6.0, 5.0)) {
                if (e is LivingEntity && e != player) {
                    e.noDamageTicks = 0
                    e.damage(30.0, player)
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