package org.ReDiego0.bastionCore.manager

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.combat.ActiveMission
import org.ReDiego0.bastionCore.combat.MissionType
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom

class GameManager(private val plugin: BastionCore) {

    private val activeGames = ConcurrentHashMap<String, ActiveMission>()
    private val bossBars = ConcurrentHashMap<String, org.bukkit.boss.BossBar>()

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
        for (i in 0..10) {
            val x = center.blockX + ThreadLocalRandom.current().nextInt(-radius, radius)
            val z = center.blockZ + ThreadLocalRandom.current().nextInt(-radius, radius)
            val y = world.getHighestBlockYAt(x, z)
            return Location(world, x.toDouble() + 0.5, y.toDouble() + 1, z.toDouble() + 0.5)
        }
        return null
    }

    fun addProgress(worldName: String, amount: Int = 1) {
        val mission = activeGames[worldName] ?: return

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

    private fun getBarTitle(mission: ActiveMission): String {
        return when (mission.type) {
            MissionType.HUNT -> "§cObjetivo: ${mission.targetId}"
            MissionType.EXTERMINATION -> "§cAmenaza Restante: ${mission.requiredAmount - mission.currentProgress}"
            MissionType.GATHER -> "§aRecolectado: ${mission.currentProgress} / ${mission.requiredAmount}"
        }
    }

    fun handleVictory(worldName: String) {
        val mission = activeGames[worldName] ?: return
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