package org.ReDiego0.bastionCore.manager

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.combat.Role
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.cos
import kotlin.math.sin

class RoleManager(private val plugin: BastionCore) {

    fun activateUltimate(player: Player, role: Role) {
        when (role) {
            Role.VANGUARDIA -> activateVanguard(player)
            Role.RASTREADOR -> activateHunter(player)
            Role.CENTINELA -> activateRaider(player)
            Role.RECLUTA -> activateRecruit(player)
        }
    }

    private fun activateVanguard(player: Player) {
        player.world.playSound(player.location, Sound.ITEM_TOTEM_USE, 1f, 0.8f)
        player.world.playSound(player.location, Sound.BLOCK_ANVIL_LAND, 0.5f, 0.5f)

        player.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, 300, 3))
        player.addPotionEffect(PotionEffect(PotionEffectType.ABSORPTION, 300, 9))
        player.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, 300, 0))

        for (e in player.world.getNearbyEntities(player.location, 20.0, 10.0, 20.0)) {
            if (e is Mob) {
                e.target = player
                e.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 60, 0))
            }
        }

        player.sendMessage("§6🛡 ¡BASTIÓN INQUEBRANTABLE!")
        broadcastNearby(player, "§e${player.name} está protegiendo al equipo.")

        object : BukkitRunnable() {
            var t = 0
            override fun run() {
                if (!player.isOnline || t >= 150) {
                    this.cancel()
                    return
                }
                t += 2
                val radius = 3.0
                for (i in 0..360 step 30) {
                    val rad = Math.toRadians(i.toDouble())
                    val x = cos(rad) * radius
                    val z = sin(rad) * radius
                    player.world.spawnParticle(Particle.END_ROD, player.location.add(x, 1.0, z), 1, 0.0, 0.0, 0.0, 0.0)
                }
            }
        }.runTaskTimer(plugin, 0L, 2L)
    }

    private fun activateHunter(player: Player) {
        player.world.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 1f)
        player.world.playSound(player.location, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1f, 0.5f)

        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 300, 1))
        player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, 600, 0))

        var count = 0
        for (e in player.world.getNearbyEntities(player.location, 50.0, 15.0, 50.0)) {
            if (e is Mob) {
                e.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 200, 0))
                e.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 200, 4))
                e.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 200, 1))
                count++
            }
        }

        player.sendMessage("§a🏹 ¡OJO DEL DEPREDADOR! ($count objetivos marcados)")

        object : BukkitRunnable() {
            var r = 0.0
            override fun run() {
                if (r > 15.0) {
                    this.cancel()
                    return
                }
                r += 0.5
                for (i in 0..360 step 10) {
                    val rad = Math.toRadians(i.toDouble())
                    val x = cos(rad) * r
                    val z = sin(rad) * r
                    player.world.spawnParticle(Particle.WITCH, player.location.add(x, 0.5, z), 1, 0.0, 0.0, 0.0, 0.0)
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }

    private fun activateRaider(player: Player) {
        player.world.playSound(player.location, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.5f)
        player.world.playSound(player.location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 2f)

        player.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, 240, 2))
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 240, 2))
        player.addPotionEffect(PotionEffect(PotionEffectType.HASTE, 240, 2))

        player.sendMessage("§c⚡ ¡SOBRECARGA DE ADRENALINA!")

        object : BukkitRunnable() {
            var t = 0
            override fun run() {
                if (!player.isOnline || t >= 120) { // 12s
                    this.cancel()
                    return
                }
                t += 2

                player.world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, player.location.add(0.0, 1.0, 0.0), 2, 0.2, 0.5, 0.2, 0.05)
                player.world.spawnParticle(Particle.ANGRY_VILLAGER, player.location.add(0.0, 2.0, 0.0), 1, 0.3, 0.2, 0.3, 0.0)

                if (t % 10 == 0) {
                    player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1f, 0.5f)
                }
            }
        }.runTaskTimer(plugin, 0L, 2L)
    }

    private fun activateRecruit(player: Player) {
        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)

        player.health = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)?.value ?: 20.0
        player.foodLevel = 20
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 100, 1))

        player.world.spawnParticle(Particle.HEART, player.location.add(0.0, 2.0, 0.0), 10, 0.5, 0.5, 0.5)
        player.sendMessage("§f❤ ¡Salud restaurada!")
    }

    private fun broadcastNearby(player: Player, msg: String) {
        for (p in player.world.getNearbyPlayers(player.location, 30.0)) {
            p.sendMessage(msg)
        }
    }
}