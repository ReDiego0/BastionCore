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

class YumiHandler(private val plugin: BastionCore) {

    fun handleRightClick(player: Player) {
        if (plugin.cooldownManager.isOnCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY)) return
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY, 6.0)

        val direction = player.location.direction.clone().setY(0).normalize().multiply(-1.8)
        player.velocity = direction.setY(0.6)

        player.world.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.5f)
        player.world.playSound(player.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 0.8f)

        player.world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, player.location, 10, 0.5, 0.1, 0.5, 0.05)
        player.world.spawnParticle(Particle.EXPLOSION, player.location, 1)

        player.sendMessage("§7💨 Evasión Explosiva")
    }

    fun handlePrimary(player: Player) {
        if (plugin.cooldownManager.isOnCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY)) return
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 15.0)

        player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 45, 10, false, false))

        player.playSound(player.location, Sound.ITEM_CROSSBOW_LOADING_START, 1f, 0.5f)
        player.sendMessage("§6⚠ Tensando Yumi...")

        object : BukkitRunnable() {
            var chargeTicks = 0
            val maxCharge = 40

            override fun run() {
                if (!player.isOnline || player.isDead) {
                    this.cancel()
                    return
                }

                val eyeLoc = player.eyeLocation
                val target = eyeLoc.clone().add(eyeLoc.direction.multiply(2))

                player.world.spawnParticle(Particle.FLAME, target, 1, 0.1, 0.1, 0.1, 0.05)
                player.world.spawnParticle(Particle.PORTAL, player.location.add(0.0, 1.0, 0.0), 5, 0.5, 0.5, 0.5, 1.0)

                if (chargeTicks % 5 == 0) {
                    val pitch = 0.5f + (chargeTicks.toFloat() / maxCharge.toFloat())
                    player.playSound(player.location, Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 0.5f, pitch)
                }

                chargeTicks++

                if (chargeTicks >= maxCharge) {
                    fireDragonPiercer(player)
                    this.cancel()
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }

    private fun fireDragonPiercer(player: Player) {
        player.world.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.5f)
        player.world.playSound(player.location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 2f)
        player.world.playSound(player.location, Sound.ENTITY_WARDEN_SONIC_BOOM, 1f, 1.5f)

        val recoil = player.location.direction.multiply(-0.5).setY(0.1)
        player.velocity = recoil

        val start = player.eyeLocation
        val dir = start.direction.normalize()
        val range = 60

        val hitEntities = mutableSetOf<Int>()

        object : BukkitRunnable() {
            var distance = 0.0

            override fun run() {
                if (distance >= range) {
                    this.cancel()
                    return
                }

                for (i in 0..4) {
                    distance += 1.0
                    if (distance > range) break

                    val point = start.clone().add(dir.clone().multiply(distance))

                    player.world.spawnParticle(Particle.LAVA, point, 1)
                    player.world.spawnParticle(Particle.SOUL_FIRE_FLAME, point, 2, 0.1, 0.1, 0.1, 0.02)
                    player.world.spawnParticle(Particle.SONIC_BOOM, point, 1)

                    if (point.block.type.isSolid) {
                        player.world.createExplosion(point, 2f, false, false)
                        player.world.spawnParticle(Particle.FLASH, point, 5)
                        this.cancel()
                        return
                    }

                    for (e in player.world.getNearbyEntities(point, 1.5, 1.5, 1.5)) {
                        if (e is LivingEntity && e != player && !hitEntities.contains(e.entityId)) {
                            hitEntities.add(e.entityId)

                            e.noDamageTicks = 0
                            e.damage(55.0, player)
                            e.fireTicks = 100

                            player.world.spawnParticle(Particle.EXPLOSION, e.location.add(0.0,1.0,0.0), 1)
                            player.playSound(e.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 0.5f)

                            e.velocity = dir.clone().multiply(0.8).setY(0.3)
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }
}