package org.ReDiego0.bastionCore.combat.weapons

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.manager.CooldownManager
import org.bukkit.Location
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
                player.world.spawnParticle(Particle.WAX_ON, player.location.add(0.0, 1.0, 0.0), 2, 0.5, 0.5, 0.5, 0.0)
            }
        }.runTaskTimer(plugin, 0L, 5L)
    }

    fun handlePrimary(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 12.0)

        val origin = player.eyeLocation
        val direction = origin.direction.normalize()
        val range = 7.0

        player.playSound(player.location, Sound.ITEM_TRIDENT_THROW, 1f, 0.5f)
        player.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1f, 0.8f)

        var hitEntity: LivingEntity? = null
        var hitLocation: Location? = null

        var dist = 0.0
        while (dist <= range) {
            val currentLoc = origin.clone().add(direction.clone().multiply(dist))

            player.world.spawnParticle(Particle.CRIT, currentLoc, 1, 0.0, 0.0, 0.0, 0.0)
            player.world.spawnParticle(Particle.FLASH, currentLoc, 0) // Destello rápido

            if (currentLoc.block.type.isSolid) {
                hitLocation = currentLoc
                break
            }

            val nearby = currentLoc.world?.getNearbyEntities(currentLoc, 0.8, 0.8, 0.8)
            val target = nearby?.firstOrNull { it is LivingEntity && it != player } as? LivingEntity

            if (target != null) {
                hitEntity = target
                hitLocation = target.location.add(0.0, 1.0, 0.0) // Centro del cuerpo
                break
            }

            dist += 0.5
        }

        if (hitLocation == null) {
            hitLocation = origin.clone().add(direction.clone().multiply(range))
        }

        createDevastatingExplosion(player, hitLocation!!, hitEntity)
    }

    private fun createDevastatingExplosion(player: Player, location: Location, directHit: LivingEntity?) {
        player.world.spawnParticle(Particle.EXPLOSION_EMITTER, location, 1)
        player.world.spawnParticle(Particle.LAVA, location, 10, 0.5, 0.5, 0.5, 0.1)
        player.world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f)
        player.world.playSound(location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 1.2f)

        if (directHit != null) {
            directHit.noDamageTicks = 0
            directHit.damage(25.0, player)
            directHit.velocity = player.location.direction.multiply(1.2).setY(0.4)
        }

        for (e in location.world!!.getNearbyEntities(location, 4.0, 4.0, 4.0)) {
            if (e is LivingEntity && e != player && e != directHit) {
                e.damage(15.0, player)

                val knockback = e.location.toVector().subtract(location.toVector()).normalize().multiply(1.0).setY(0.5)
                e.velocity = knockback
            }
        }
    }

    fun triggerExplosiveCounter(player: Player) {
        player.world.createExplosion(player.location, 0f, false)
        player.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f)

        val front = player.location.add(player.location.direction.multiply(2))

        for (e in player.world.getNearbyEntities(front, 4.0, 4.0, 4.0)) {
            if (e is LivingEntity && e != player) {
                e.damage(25.0, player)
                e.velocity = player.location.direction.multiply(1.8).setY(0.5)
            }
        }
        player.sendMessage("§6¡Contraataque Explosivo!")
        plugin.combatManager.removeBlocking(player.uniqueId)
    }
}