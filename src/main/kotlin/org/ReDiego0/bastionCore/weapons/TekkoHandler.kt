package org.ReDiego0.bastionCore.combat.weapons

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.manager.CooldownManager
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class TekkoHandler(private val plugin: BastionCore) {

    private val comboMap = ConcurrentHashMap<UUID, Int>()
    private val lastHitMap = ConcurrentHashMap<UUID, Long>()

    fun handleRightClick(player: Player) {
        if (plugin.cooldownManager.isOnCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY)) return
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY, 12.0)

        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 60, 1, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, 40, 0, false, false))

        player.world.playSound(player.location, Sound.ENTITY_WARDEN_HEARTBEAT, 1f, 1.5f)
        player.world.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 2f)

        player.world.spawnParticle(Particle.ANGRY_VILLAGER, player.location.add(0.0, 1.0, 0.0), 5, 0.3, 0.3, 0.3, 0.0)
        player.world.spawnParticle(Particle.CRIT, player.location, 10, 0.5, 1.0, 0.5, 0.1)

        player.sendMessage("§c⚡ ¡Modo Frenesí Activado!")
    }

    fun handlePrimary(player: Player) {
        if (plugin.cooldownManager.isOnCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY)) return

        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 0.5)
        val lastTime = lastHitMap.getOrDefault(player.uniqueId, 0L)
        var combo = comboMap.getOrDefault(player.uniqueId, 0)

        if (System.currentTimeMillis() - lastTime > 1500) {
            combo = 0
        }

        combo++

        comboMap[player.uniqueId] = combo
        lastHitMap[player.uniqueId] = System.currentTimeMillis()

        performAttack(player, combo)

        if (combo >= 3) {
            comboMap[player.uniqueId] = 0
            plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 1.5)
        }
    }

    private fun performAttack(player: Player, comboStep: Int) {
        val range = 3.5
        val damage = when (comboStep) {
            1 -> 4.0
            2 -> 5.0
            3 -> 12.0
            else -> 4.0
        }

        val dir = player.location.direction.clone().setY(0).normalize()
        player.velocity = dir.multiply(0.4).setY(0.1)

        player.world.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1.5f + (comboStep * 0.1f))

        val start = player.eyeLocation
        val direction = start.direction

        val particleLoc = start.clone().add(direction.multiply(1.5))
        player.world.spawnParticle(Particle.SWEEP_ATTACK, particleLoc, 1)

        var hit = false

        for (i in 1..3) {
            val point = start.clone().add(direction.clone().multiply(i.toDouble()))
            val entities = player.world.getNearbyEntities(point, 1.0, 1.0, 1.0)

            for (entity in entities) {
                if (entity is LivingEntity && entity != player) {
                    if (entity.uniqueId == player.uniqueId) continue

                    entity.damage(damage, player)
                    entity.noDamageTicks = 5

                    if (comboStep == 3) {
                        performFinisher(player, entity)
                    } else {
                        player.world.playSound(entity.location, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1f, 1.5f)
                        player.world.spawnParticle(Particle.CRIT, entity.location.add(0.0, 1.0, 0.0), 5, 0.2, 0.2, 0.2, 0.1)
                    }
                    hit = true
                    break
                }
            }
            if (hit) break
        }

        if (!hit && comboStep == 3) {
            player.world.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_NODAMAGE, 1f, 0.5f)
        }
    }

    private fun performFinisher(player: Player, target: LivingEntity) {

        val knockback = player.location.direction.setY(0).normalize().multiply(1.5).setY(0.4)
        target.velocity = knockback

        player.world.playSound(target.location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 2f)
        player.world.playSound(target.location, Sound.BLOCK_ANVIL_LAND, 0.5f, 0.5f)

        target.world.spawnParticle(Particle.EXPLOSION, target.location.add(0.0, 1.0, 0.0), 1)
        target.world.spawnParticle(Particle.FLASH, target.location.add(0.0, 1.0, 0.0), 1)

        target.world.spawnParticle(Particle.CLOUD, target.location, 10, 0.5, 0.5, 0.5, 0.1)

        player.sendMessage("§6👊 ¡GOLPE FINAL!")
    }
}