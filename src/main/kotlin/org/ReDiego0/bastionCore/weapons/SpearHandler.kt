package org.ReDiego0.bastionCore.combat.weapons

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.manager.CooldownManager
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

class SpearHandler(private val plugin: BastionCore) {

    fun handleRightClick(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY, 15.0)

        plugin.combatManager.setBlocking(player.uniqueId, 100) // 5s activo
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 0.8f)
        player.sendMessage("§eGuardia Reactiva Activa")
        player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 100, 2))
        player.world.spawnParticle(Particle.END_ROD, player.location.add(0.0, 1.0, 0.0), 10, 0.5, 0.5, 0.5, 0.0)
    }

    fun handlePrimary(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 10.0)

        player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1f)
        player.velocity = Vector(0.0, 2.5, 0.0)

        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            player.velocity = player.location.direction.multiply(3).setY(-2.0)
            player.fallDistance = 0f
            player.world.spawnParticle(Particle.SOUL_FIRE_FLAME, player.location, 20)

            plugin.server.scheduler.runTaskLater(plugin, Runnable {
                player.world.createExplosion(player.location, 0f)
                for (e in player.world.getNearbyEntities(player.location, 4.0, 3.0, 4.0)) {
                    if (e is LivingEntity && e != player) {
                        e.damage(25.0, player)
                        e.velocity = e.location.toVector().subtract(player.location.toVector()).normalize().multiply(0.5).setY(0.5)
                    }
                }
            }, 5L)
        }, 12L)
    }

    fun triggerExplosiveCounter(player: Player) {
        player.world.createExplosion(player.location, 0f, false)
        player.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f)
        val front = player.location.add(player.location.direction.multiply(2))
        for (e in player.world.getNearbyEntities(front, 4.0, 4.0, 4.0)) {
            if (e is LivingEntity && e != player) {
                e.damage(20.0, player)
                e.velocity = player.location.direction.multiply(1.5).setY(0.5)
            }
        }
        player.sendMessage("§6¡Contraataque!")
        plugin.combatManager.removeBlocking(player.uniqueId)
    }
}