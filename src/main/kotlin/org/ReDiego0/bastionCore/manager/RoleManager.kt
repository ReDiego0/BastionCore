package org.ReDiego0.bastionCore.manager

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.combat.Role
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class RoleManager(private val plugin: BastionCore) {

    fun activateUltimate(player: Player, role: Role) {
        when (role) {
            Role.SAMURAI -> activateSamurai(player)
            Role.ORACULO -> activateOracle(player)
            Role.KENSAI -> activateKensai(player)
            Role.VAGABUNDO -> activateVagrant(player)
        }
    }

    private fun activateSamurai(player: Player) {
        player.world.playSound(player.location, Sound.ITEM_SHIELD_BLOCK, 1f, 0.5f)
        player.world.playSound(player.location, Sound.BLOCK_ANVIL_LAND, 0.5f, 0.5f)
        player.world.playSound(player.location, Sound.ENTITY_IRON_GOLEM_HURT, 1f, 0.1f)

        player.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, 160, 4))
        player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 160, 2))
        player.addPotionEffect(PotionEffect(PotionEffectType.ABSORPTION, 160, 9))

        for (e in player.world.getNearbyEntities(player.location, 20.0, 10.0, 20.0)) {
            if (e is Mob) {
                e.target = player
                e.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 60, 0))
            }
        }

        player.sendMessage("§6🛡 ¡DEFENSA ABSOLUTA!")
        broadcastNearby(player, "§e${player.name} se ha convertido en una fortaleza.")

        object : BukkitRunnable() {
            var t = 0
            override fun run() {
                if (!player.isOnline || t >= 160) {
                    this.cancel()
                    return
                }

                val loc = player.location
                val radius = 3.5

                for (i in 0 until 3) {
                    val angle = ((t * 10 + i * 120) % 360).toDouble()
                    val rad = Math.toRadians(angle)
                    val x = cos(rad) * radius
                    val z = sin(rad) * radius

                    player.world.spawnParticle(Particle.CRIT, loc.clone().add(x, 0.5 + (t % 20) * 0.1, z), 1, 0.0, 0.0, 0.0, 0.0)
                    player.world.spawnParticle(Particle.FALLING_DUST, loc.clone().add(x*0.5, 1.0, z*0.5), 1, 0.0, 0.0, 0.0, 0.0, player.location.block.blockData)
                }

                t += 2
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }

    private fun activateOracle(player: Player) {
        player.world.playSound(player.location, Sound.BLOCK_BEACON_ACTIVATE, 1f, 2f)
        player.world.playSound(player.location, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 2f, 0.5f)

        val allies = player.world.getNearbyPlayers(player.location, 20.0)
        allies.add(player)

        for (ally in allies) {
            ally.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 200, 2))
            ally.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 100, 2))
            ally.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, 60, 4))

            ally.sendMessage("§b👁 Visión Futura otorgada por ${player.name}")
            ally.playSound(ally.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 2f)
        }

        player.sendMessage("§b👁 ¡EL DESTINO HA SIDO REESCRITO!")

        object : BukkitRunnable() {
            var radius = 0.5
            override fun run() {
                if (radius > 15.0) {
                    this.cancel()
                    return
                }

                val loc = player.location.add(0.0, 1.0, 0.0)
                for (i in 0..360 step 15) {
                    val rad = Math.toRadians(i.toDouble())
                    val x = cos(rad) * radius
                    val z = sin(rad) * radius

                    player.world.spawnParticle(Particle.END_ROD, loc.clone().add(x, 0.0, z), 1, 0.0, 0.0, 0.0, 0.0)
                    if (i % 30 == 0) {
                        player.world.spawnParticle(Particle.SOUL_FIRE_FLAME, loc.clone().add(x, 0.0, z), 1, 0.0, 0.05, 0.0, 0.0)
                    }
                }
                radius += 0.75
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }

    private fun activateKensai(player: Player) {
        val targets = player.world.getNearbyEntities(player.location, 15.0, 10.0, 15.0)
            .filterIsInstance<LivingEntity>()
            .filter { it != player && it !is Player }

        if (targets.isEmpty()) {
            player.sendMessage("§cNo hay objetivos para el Corte Dimensional.")
            plugin.playerDataManager.getData(player.uniqueId)?.ultimateCharge = 100.0
            return
        }

        player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 40, 0, false, false))
        player.world.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f)
        player.world.spawnParticle(Particle.CLOUD, player.location, 20, 0.5, 1.0, 0.5, 0.1)

        player.sendMessage("§3⚔ ¡CORTE DIMENSIONAL!")

        object : BukkitRunnable() {
            var step = 0
            override fun run() {
                if (step >= 10) {
                    player.world.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 2f)
                    player.world.playSound(player.location, Sound.ITEM_TRIDENT_THUNDER, 1f, 2f)
                    this.cancel()
                    return
                }

                for (target in targets) {
                    if (target.isValid) {
                        target.world.spawnParticle(Particle.SWEEP_ATTACK, target.location.add(0.0, 1.0, 0.0), 1)
                        val rX = (Random.nextDouble() - 0.5) * 2
                        val rY = (Random.nextDouble() - 0.5) * 2
                        val rZ = (Random.nextDouble() - 0.5) * 2
                        target.world.spawnParticle(Particle.CRIT, target.location.add(rX, 1.0 + rY, rZ), 3)
                        target.world.playSound(target.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.5f)

                        if (step == 9) {
                            target.damage(50.0, player)
                            target.world.spawnParticle(Particle.FLASH, target.location, 1)
                        } else {
                            target.velocity = Vector(0, 0, 0)
                        }
                    }
                }
                step++
            }
        }.runTaskTimer(plugin, 5L, 2L)
    }

    private fun activateVagrant(player: Player) {
        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
        player.health = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)?.value ?: 20.0
        player.foodLevel = 20
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 100, 1))

        player.world.spawnParticle(Particle.HEART, player.location.add(0.0, 2.0, 0.0), 10, 0.5, 0.5, 0.5)
        player.sendMessage("§f❤ Has recuperado el aliento.")
    }

    private fun broadcastNearby(player: Player, msg: String) {
        for (p in player.world.getNearbyPlayers(player.location, 30.0)) {
            p.sendMessage(msg)
        }
    }
}