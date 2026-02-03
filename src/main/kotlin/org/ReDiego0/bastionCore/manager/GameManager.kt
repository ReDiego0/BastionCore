package org.ReDiego0.bastionCore.manager

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.combat.ActiveMission
import org.ReDiego0.bastionCore.combat.MissionType
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom

class GameManager(private val plugin: BastionCore) {

    private val activeGames = ConcurrentHashMap<String, ActiveMission>()
    private val bossBars = ConcurrentHashMap<String, org.bukkit.boss.BossBar>()
    private var gameLoopTask: BukkitRunnable? = null
    private val deathCooldowns = ConcurrentHashMap<UUID, Long>()

    init {
        startGameLoop()
    }

    private fun startGameLoop() {
        gameLoopTask = object : BukkitRunnable() {
            override fun run() {
                for (mission in activeGames.values) {
                    updateMissionTimer(mission)
                }
            }
        }
        gameLoopTask?.runTaskTimer(plugin, 20L, 20L)
    }

    private fun updateMissionTimer(mission: ActiveMission) {
        mission.timeElapsed++

        val timeLeft = mission.timeLimitSeconds - mission.timeElapsed

        if (timeLeft == 300) {
            broadcastToWorld(mission.worldName, "§c[!] Quedan 5 minutos para el fallo de misión.")
        }
        if (timeLeft == 60) {
            broadcastToWorld(mission.worldName, "§c[!] ¡Queda 1 minuto!")
        }

        if (timeLeft <= 0) {
            handleDefeat(mission.worldName, "Tiempo Agotado")
        }
    }

    fun handlePlayerFaint(player: Player, mission: ActiveMission) {
        if (mission.isEnded) return

        val lastDeath = deathCooldowns.getOrDefault(player.uniqueId, 0L)
        if (System.currentTimeMillis() - lastDeath < 2000) return
        deathCooldowns[player.uniqueId] = System.currentTimeMillis()

        mission.currentLives--
        val livesLeft = mission.currentLives

        broadcastToWorld(mission.worldName, "§c⚠ ${player.name} ha caído. Vidas restantes: $livesLeft/${mission.maxLives}")

        if (livesLeft < 0) {
            handleDefeat(mission.worldName, "Vidas Agotadas")
        } else {
            val world = Bukkit.getWorld(mission.worldName)
            if (world != null) {
                player.teleport(world.spawnLocation)
                player.playSound(player.location, Sound.BLOCK_BEACON_DEACTIVATE, 1f, 0.5f)
                player.sendTitle("§c¡Has caído!", "§7Regresando al campamento...", 5, 40, 10)

                player.health = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)?.value ?: 20.0
                player.foodLevel = 20

                player.activePotionEffects.forEach { player.removePotionEffect(it.type) }
            }
        }
    }

    fun handleDefeat(worldName: String, reason: String) {
        val mission = activeGames[worldName] ?: return
        if (mission.isEnded) return
        mission.isEnded = true

        activeGames.remove(worldName)
        bossBars[worldName]?.removeAll()
        bossBars.remove(worldName)

        broadcastToWorld(worldName, "§4█ MISIÓN FALLIDA █")
        broadcastToWorld(worldName, "§cMotivo: $reason")

        val world = Bukkit.getWorld(worldName)
        if (world != null) {
            for (player in world.players) {
                player.sendTitle("§4MISIÓN FALLIDA", "§c$reason", 10, 100, 20)
                player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
            }
        }

        object : BukkitRunnable() {
            override fun run() {
                plugin.instanceManager.unloadInstance(worldName)
            }
        }.runTaskLater(plugin, 100L)
    }


    fun startGame(mission: ActiveMission, spawnRadius: Int) {
        activeGames[mission.worldName] = mission
        val world = Bukkit.getWorld(mission.worldName) ?: return

        val barTitle = getBarTitle(mission)
        val bar = Bukkit.createBossBar(barTitle, BarColor.RED, BarStyle.SEGMENTED_10)
        bar.progress = 0.0
        val player = Bukkit.getPlayer(mission.leaderId)
        if (player != null) bar.addPlayer(player)
        bossBars[mission.worldName] = bar

        if (mission.type == MissionType.HUNT || mission.type == MissionType.EXTERMINATION) {
            spawnTargets(world, mission, spawnRadius)
        } else {
            plugin.logger.info("Misión GATHER iniciada en ${mission.worldName}")
        }
    }

    private fun spawnTargets(world: org.bukkit.World, mission: ActiveMission, radius: Int) {
        val center = world.spawnLocation
        val amountToSpawn = mission.requiredAmount

        plugin.logger.info("Spawneando $amountToSpawn x ${mission.targetId} en radio $radius")

        var spawnedCount = 0
        for (i in 0 until amountToSpawn) {
            val loc = getSafeRandomLocation(center, radius)
            if (loc != null) {
                SpawnManager.spawnBoss(loc, mission.targetId)
                spawnedCount++
            }
        }

        if (spawnedCount < amountToSpawn) {
            plugin.logger.warning("No se pudieron encontrar lugares seguros para todos los mobs ($spawnedCount/$amountToSpawn)")
        }
    }

    private fun getSafeRandomLocation(center: Location, radius: Int): Location? {
        val world = center.world ?: return null
        for (i in 0..15) {
            val x = center.blockX + ThreadLocalRandom.current().nextInt(-radius, radius)
            val z = center.blockZ + ThreadLocalRandom.current().nextInt(-radius, radius)

            val y = world.getHighestBlockYAt(x, z)

            val block = world.getBlockAt(x, y, z)
            val blockAbove = world.getBlockAt(x, y + 1, z)

            if (!block.isLiquid && !blockAbove.isLiquid) {
                return Location(world, x.toDouble() + 0.5, y.toDouble() + 1.0, z.toDouble() + 0.5)
            }
        }
        return center.clone().add(0.0, 1.0, 0.0)
    }

    fun addProgress(worldName: String, amount: Int = 1) {
        val mission = activeGames[worldName] ?: return
        if (mission.isEnded) return

        mission.currentProgress += amount

        val bar = bossBars[worldName]
        if (bar != null) {
            val progress = mission.currentProgress.toDouble() / mission.requiredAmount.toDouble()
            bar.progress = progress.coerceIn(0.0, 1.0)
            bar.setTitle(getBarTitle(mission))
        }

        if (mission.currentProgress >= mission.requiredAmount) {
            handleVictory(worldName)
        }
    }

    private fun broadcastToWorld(worldName: String, message: String) {
        val world = Bukkit.getWorld(worldName) ?: return
        for (p in world.players) p.sendMessage(message)
    }

    private fun getBarTitle(mission: ActiveMission): String {
        return when (mission.type) {
            MissionType.HUNT -> "§cObjetivo: ${mission.targetId}"
            MissionType.EXTERMINATION -> "§cAmenaza Restante: ${mission.requiredAmount - mission.currentProgress}"
            MissionType.GATHER -> "§aRecolectado: ${mission.currentProgress} / ${mission.requiredAmount}"
        }
    }

    fun handleVictory(worldName: String) {
        val mission = activeGames[worldName] ?: return
        if (mission.isEnded) return

        mission.isEnded = true

        val player = Bukkit.getPlayer(mission.leaderId)

        bossBars[worldName]?.removeAll()
        bossBars.remove(worldName)

        if (player != null && player.isOnline) {
            player.sendTitle("§6¡MISIÓN CUMPLIDA!", "§7Objetivos completados.", 10, 80, 20)
            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)

            val xpReward = (mission.rewardGold * 0.10).toInt().coerceAtLeast(10)
            RewardManager.grantReward(player, mission.rewardGold, emptyList(), emptyList(), xpReward)
            player.sendMessage("§e[Extracción] §fLa aeronave de recogida llegará en 15 segundos.")
        }

        object : BukkitRunnable() {
            override fun run() {
                plugin.instanceManager.unloadInstance(worldName)
                activeGames.remove(worldName)
            }
        }.runTaskLater(plugin, 20L * 15)
    }

    fun getMission(worldName: String): ActiveMission? {
        return activeGames[worldName]
    }
}