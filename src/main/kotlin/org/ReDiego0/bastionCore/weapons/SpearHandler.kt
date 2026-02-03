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

class SpearHandler(private val plugin: BastionCore) {

    fun handleRightClick(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY, 15.0)

        plugin.combatManager.setBlocking(player.uniqueId, 100)
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 0.8f)
        player.sendMessage("§eGuardia Reactiva Activa")
        player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 100, 2))
        player.world.spawnParticle(Particle.END_ROD, player.location.add(0.0, 1.0, 0.0), 10, 0.5, 0.5, 0.5, 0.0)
        val id = player.uniqueId
        object : BukkitRunnable() {
            var t = 0
            override fun run() {
                if (t >= 100 || !plugin.combatManager.isBlocking(id)) {
                    this.cancel()
                    return
                }
                t += 5
                player.world.spawnParticle(Particle.END_ROD, player.location.add(0.0, 1.0, 0.0), 2, 0.5, 0.5, 0.5, 0.0)
            }
        }.runTaskTimer(plugin, 0L, 5L)
    }

    fun handlePrimary(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 10.0)

        val loc = player.location

        object : BukkitRunnable() {
            var hits = 0
            override fun run() {
                if (!player.isOnline || hits >= 10) {
                    this.cancel()
                    player.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.5f)
                    return
                }
                hits++
                player.teleport(loc.setDirection(player.location.direction))

                val eye = player.eyeLocation.subtract(0.0, 0.2, 0.0)
                val target = eye.clone().add(eye.direction.multiply(3.5))
                player.world.spawnParticle(Particle.CRIT, target, 5, 0.2, 0.2, 0.2, 0.1)
                player.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 2.0f)

                val damageLoc = player.location.add(player.location.direction.multiply(2.5))
                for (e in player.world.getNearbyEntities(damageLoc, 1.5, 1.5, 1.5)) {
                    if (e is LivingEntity && e != player) {
                        e.noDamageTicks = 0
                        e.damage(4.0, player)
                        e.velocity = player.location.direction.multiply(0.2)
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L)
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