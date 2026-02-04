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
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

class BowHandler(private val plugin: BastionCore) {

    val EXPLOSIVE_KEY = NamespacedKey(plugin, "is_explosive_arrow")

    fun handleRightClick(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY, 8.0)

        player.playSound(player.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 0.8f)

        val arrow = player.launchProjectile(Arrow::class.java)
        arrow.velocity = arrow.velocity.multiply(1.5)
        arrow.isCritical = true
        arrow.persistentDataContainer.set(EXPLOSIVE_KEY, PersistentDataType.BYTE, 1.toByte())
        player.world.spawnParticle(Particle.SMOKE, player.eyeLocation.add(player.location.direction), 5, 0.1, 0.1, 0.1, 0.05)
    }

    fun handlePrimary(player: Player) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 18.0)

        player.sendMessage("§6⚠ Cargando Perforador...")

        player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 45, 3, false, false))
        player.playSound(player.location, Sound.ITEM_CROSSBOW_LOADING_MIDDLE, 1f, 0.5f)

        object : BukkitRunnable() {
            var chargeTicks = 0
            val maxCharge = 40

            override fun run() {
                if (!player.isOnline || player.isDead) {
                    this.cancel()
                    return
                }

                if (chargeTicks < maxCharge) {
                    val loc = player.eyeLocation.add(player.location.direction.multiply(0.8))
                    player.world.spawnParticle(Particle.FLAME, loc, 2, 0.1, 0.1, 0.1, 0.0)
                    player.world.spawnParticle(Particle.SMOKE, loc, 1, 0.1, 0.1, 0.1, 0.0)

                    if (chargeTicks % 5 == 0) {
                        val pitch = 0.5f + (chargeTicks.toFloat() / maxCharge.toFloat())
                        player.playSound(player.location, Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 0.5f, pitch)
                    }

                    chargeTicks++
                } else {
                    fireDragonPiercer(player)
                    this.cancel()
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }

    private fun fireDragonPiercer(player: Player) {
        player.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 2f)
        player.playSound(player.location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 0.5f)
        player.playSound(player.location, Sound.ITEM_TRIDENT_THUNDER, 1f, 1f)

        player.velocity = player.location.direction.multiply(-0.8).setY(0.2)

        val start = player.eyeLocation
        val dir = start.direction.normalize()
        val range = 50

        for (i in 0..range step 1) {
            val point = start.clone().add(dir.clone().multiply(i.toDouble()))

            player.world.spawnParticle(Particle.FIREWORK, point, 1, 0.0, 0.0, 0.0, 0.0)
            player.world.spawnParticle(Particle.FLAME, point, 2, 0.1, 0.1, 0.1, 0.01)
            player.world.spawnParticle(Particle.LAVA, point, 1, 0.0, 0.0, 0.0, 0.0)

            val entities = player.world.getNearbyEntities(point, 1.2, 1.2, 1.2)
            for (e in entities) {
                if (e is LivingEntity && e != player) {
                    e.noDamageTicks = 0
                    e.damage(45.0, player)
                    e.fireTicks = 100

                    player.world.spawnParticle(Particle.EXPLOSION, e.location.add(0.0, 1.0, 0.0), 1)
                    player.playSound(e.location, Sound.BLOCK_ANVIL_LAND, 0.5f, 2f)
                }
            }

            if (point.block.type.isSolid) {
                player.world.createExplosion(point, 2f, false)
                player.world.spawnParticle(Particle.FLASH, point, 1)
                break
            }
        }
    }
}