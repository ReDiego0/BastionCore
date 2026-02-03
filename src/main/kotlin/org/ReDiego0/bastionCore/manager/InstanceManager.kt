package org.ReDiego0.bastionCore.manager

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.utils.FileUtils
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import java.io.File
import java.util.*
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

        if (world != null) {
            world.isAutoSave = false

            world.setGameRule(org.bukkit.GameRule.DO_MOB_SPAWNING, false)
            world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, false)
            world.time = 6000

            activeInstances[instanceName] = UUID.randomUUID()
            plugin.worldGuardManager.applyInstanceRules(world)
        }

        return world
    }

    fun unloadInstance(worldName: String) {
        val world = Bukkit.getWorld(worldName)
        if (world == null) {
            activeInstances.remove(worldName)
            deleteWorldFiles(worldName)
            return
        }

        val citadel = Bukkit.getWorld(plugin.citadelWorldName) ?: Bukkit.getWorlds()[0]
        for (player in world.players) {
            player.teleport(citadel.spawnLocation)
            player.sendMessage("§e[Misión] §fInstancia cerrada. Regresando a base.")
        }

        for (entity in world.entities) {
            if (entity !is org.bukkit.entity.Player) {
                entity.remove()
            }
        }

        activeInstances.remove(worldName)

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            attemptUnload(worldName, 1)
        }, 60L)
    }

    private fun attemptUnload(worldName: String, attempt: Int) {
        val world = Bukkit.getWorld(worldName)

        if (world == null) {
            deleteWorldFiles(worldName)
            return
        }

        if (Bukkit.unloadWorld(world, false)) {
            plugin.logger.info("Mundo descargado con éxito ($worldName). Eliminando archivos...")
            deleteWorldFiles(worldName)
        } else {
            if (attempt < 3) {
                plugin.logger.warning("Fallo al descargar $worldName (Intento $attempt/3). Reintentando en 5s...")
                Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    attemptUnload(worldName, attempt + 1)
                }, 100L)
            } else {
                plugin.logger.severe("IMPOSIBLE descargar el mundo $worldName tras 3 intentos. Se quedará cargado hasta el reinicio.")
                for (chunk in world.loadedChunks) {
                    chunk.unload(false)
                }
            }
        }
    }

    private fun deleteWorldFiles(worldName: String) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val dir = File(Bukkit.getWorldContainer(), worldName)
            if (!dir.exists()) return@Runnable
            if (FileUtils.deleteDirectory(dir)) {
                plugin.logger.info("Archivos de instancia eliminados: $worldName")
            } else {
                plugin.logger.warning("No se pudo borrar la carpeta $worldName inmediatamente (probablemente bloqueo de SO). Se borrará en el próximo reinicio o limpieza.")
            }
        })
    }

    fun cleanupAll() {
        for (worldName in activeInstances.keys) {
            unloadInstance(worldName)
        }
    }
}