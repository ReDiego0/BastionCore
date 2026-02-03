package org.ReDiego0.bastionCore.task

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.combat.MissionType
import org.ReDiego0.bastionCore.utils.CompassUtils
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class TrackingTask(private val plugin: BastionCore) : BukkitRunnable() {
    private val gatherCache = HashMap<String, Location>()
    private var tickCounter = 0

    override fun run() {
        tickCounter++

        for (player in Bukkit.getOnlinePlayers()) {
            if (player.gameMode == GameMode.SPECTATOR) continue
            val worldName = player.world.name
            if (!worldName.startsWith("inst_")) continue

            val mission = plugin.gameManager.getMission(worldName) ?: continue
            var targetLoc: Location? = null

            when (mission.type) {
                MissionType.HUNT, MissionType.EXTERMINATION -> {
                    val nearest = findNearestMob(player, mission.targetId)
                    if (nearest != null) targetLoc = nearest.location
                }

                MissionType.GATHER -> {
                    val droppedItem = findDroppedItem(player, mission.targetId)
                    if (droppedItem != null) {
                        targetLoc = droppedItem.location
                    } else {
                        val cachedLoc = gatherCache[player.uniqueId.toString()]

                        if (cachedLoc != null && cachedLoc.world != player.world) {
                            gatherCache.remove(player.uniqueId.toString()) // Limpiar caché vieja
                        }

                        if (tickCounter % 20 == 0) {
                            val nearestBlock = scanNearbyBlocks(player, mission.targetId, 25)
                            if (nearestBlock != null) {
                                gatherCache[player.uniqueId.toString()] = nearestBlock
                            } else {
                                gatherCache.remove(player.uniqueId.toString())
                            }
                        }
                        targetLoc = gatherCache[player.uniqueId.toString()]
                    }
                }
            }

            if (targetLoc != null && targetLoc.world == player.world) {
                val compass = CompassUtils.getSkyrimCompass(player, targetLoc)
                sendActionBar(player, compass)
            } else {
                if (mission.type == MissionType.GATHER) {
                    sendActionBar(player, "§7Explora para encontrar recursos...")
                } else {
                    sendActionBar(player, "§7Rastreando objetivo...")
                }
            }
        }
    }

    private fun sendActionBar(player: Player, msg: String) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(msg))
    }

    private fun findNearestMob(player: Player, targetId: String): LivingEntity? {
        var closest: LivingEntity? = null
        var minDst = Double.MAX_VALUE
        for (entity in player.world.livingEntities) {
            if (entity == player) continue
            var isMatch = false
            if (entity.type.name.equals(targetId, ignoreCase = true)) isMatch = true
            if (!isMatch && plugin.server.pluginManager.isPluginEnabled("MythicMobs")) {
                val mm = io.lumine.mythic.bukkit.MythicBukkit.inst().mobManager
                if (mm.isActiveMob(entity.uniqueId)) {
                    if (mm.getMythicMobInstance(entity).type.internalName.equals(targetId, ignoreCase = true)) {
                        isMatch = true
                    }
                }
            }

            if (isMatch) {
                val dst = player.location.distanceSquared(entity.location)
                if (dst < minDst) {
                    minDst = dst
                    closest = entity
                }
            }
        }
        return closest
    }

    private fun findDroppedItem(player: Player, targetId: String): Entity? {
        var closest: Entity? = null
        var minDst = Double.MAX_VALUE

        for (entity in player.world.getEntitiesByClass(org.bukkit.entity.Item::class.java)) {
            val itemStack = entity.itemStack
            if (itemStack.type.name.equals(targetId, ignoreCase = true)) {
                val dst = player.location.distanceSquared(entity.location)
                if (dst < minDst) {
                    minDst = dst
                    closest = entity
                }
            }
        }
        return closest
    }

    private fun scanNearbyBlocks(player: Player, materialName: String, radius: Int): Location? {
        val mat = Material.getMaterial(materialName) ?: return null
        val pLoc = player.location
        val cx = pLoc.blockX
        val cy = pLoc.blockY
        val cz = pLoc.blockZ

        for (x in cx - radius..cx + radius) {
            for (y in cy - 5..cy + 10) {
                for (z in cz - radius..cz + radius) {
                    val block = player.world.getBlockAt(x, y, z)
                    if (block.type == mat) {
                        return block.location.add(0.5, 0.5, 0.5)
                    }
                }
            }
        }
        return null
    }
}