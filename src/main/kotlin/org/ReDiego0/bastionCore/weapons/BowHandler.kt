package org.ReDiego0.bastionCore.combat.weapons

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.manager.CooldownManager
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

class BowHandler(private val plugin: BastionCore) {

    val EXPLOSIVE_KEY = NamespacedKey(plugin, "is_explosive_arrow")

    fun handleRightClick(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY, 8.0)

        player.playSound(player.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 0.8f)

        val arrow = player.launchProjectile(Arrow::class.java)
        arrow.velocity = arrow.velocity.multiply(1.5)
        arrow.isCritical = true
        arrow.persistentDataContainer.set(EXPLOSIVE_KEY, PersistentDataType.BYTE, 1.toByte())
    }

    fun handlePrimary(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 15.0)
        player.playSound(player.location, Sound.ENTITY_TNT_PRIMED, 1f, 1f)

        val start = player.eyeLocation
        val dir = start.direction.normalize()

        for (i in 0..40 step 1) {
            val point = start.clone().add(dir.clone().multiply(i.toDouble()))

            player.world.spawnParticle(Particle.END_ROD, point, 1, 0.0, 0.0, 0.0, 0.0)
            player.world.spawnParticle(Particle.FLAME, point, 1, 0.0, 0.0, 0.0, 0.0)

            for (e in player.world.getNearbyEntities(point, 1.0, 1.0, 1.0)) {
                if (e is LivingEntity && e != player) {
                    e.noDamageTicks = 0
                    e.damage(45.0, player)
                    e.fireTicks = 100
                }
            }

            if (point.block.type.isSolid) {
                player.world.createExplosion(point, 0f, false)
                break
            }
        }
        player.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 2f)
    }
}