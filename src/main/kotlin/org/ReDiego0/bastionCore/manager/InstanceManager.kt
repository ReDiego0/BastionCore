package org.ReDiego0.bastionCore.manager

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.utils.FileUtils
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InstanceManager(private val plugin: BastionCore) {
    private val activeInstances = ConcurrentHashMap<String, UUID>()

    fun createInstance(templateName: String): World? {
        val container = Bukkit.getWorldContainer()
        val source = File(container, "templates/$templateName")

        if (!source.exists()) {
            plugin.logger.severe("¡Plantilla no encontrada: $templateName!")
            return null
        }

        val uniqueID = UUID.randomUUID().toString().substring(0, 8)
        val instanceName = "inst_${templateName}_$uniqueID"
        val target = File(container, instanceName)

        plugin.logger.info("Generando instancia: $instanceName...")
        FileUtils.copyDirectory(source, target)

        val creator = WorldCreator(instanceName)
        val world = Bukkit.createWorld(creator)
        world?.isAutoSave = false

        if (world != null) {
            world.isAutoSave = false
            activeInstances[instanceName] = UUID.randomUUID()
            plugin.worldGuardManager.applyInstanceRules(world)
        }

        return world
    }


    fun unloadInstance(worldName: String) {
        val world = Bukkit.getWorld(worldName) ?: return

        val citadel = Bukkit.getWorld(plugin.citadelWorldName) ?: Bukkit.getWorlds()[0]
        for (player in world.players) {
            player.teleport(citadel.spawnLocation)
            player.sendMessage("§e[Misión] §fInstancia cerrada. Regresando a base.")
        }

        Bukkit.unloadWorld(world, false)
        activeInstances.remove(worldName)

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val dir = File(Bukkit.getWorldContainer(), worldName)
            FileUtils.deleteDirectory(dir)
            plugin.logger.info("Instancia borrada: $worldName")
        })
    }

    fun cleanupAll() {
        for (worldName in activeInstances.keys) {
            unloadInstance(worldName)
        }
    }
}