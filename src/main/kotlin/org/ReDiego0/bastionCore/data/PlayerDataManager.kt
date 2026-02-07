package org.ReDiego0.bastionCore.data

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.storage.DataStorage
import org.ReDiego0.bastionCore.storage.SqliteStorage
import org.ReDiego0.bastionCore.storage.YamlStorage
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PlayerDataManager(private val plugin: BastionCore) : Listener {

    private val dataMap = ConcurrentHashMap<UUID, PlayerData>()
    private lateinit var storage: DataStorage

    init {
        val type = plugin.config.getString("storage.type", "YAML")?.uppercase()
        storage = if (type == "SQLITE") {
            SqliteStorage(plugin)
        } else {
            YamlStorage(plugin)
        }

        storage.init()
        startAutoSave()
    }

    fun getData(uuid: UUID): PlayerData? {
        return dataMap[uuid]
    }

    fun shutdown() {
        plugin.logger.info("Guardando datos de jugadores...")
        for (data in dataMap.values) {
            storage.savePlayer(data)
        }
        storage.close()
    }

    @EventHandler
    fun onPreLogin(event: AsyncPlayerPreLoginEvent) {
        val uuid = event.uniqueId
        val name = event.name

        try {
            val data = storage.loadPlayer(uuid, name)
            dataMap[uuid] = data
        } catch (e: Exception) {
            plugin.logger.severe("Error cargando datos para $name: ${e.message}")
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        event.player.level = 0
        event.player.exp = 0f
    }

    @EventHandler
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        updateFlightPermission(event.player)
        updateWorldContext(event.player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val uuid = event.player.uniqueId
        val data = dataMap.remove(uuid)

        if (data != null) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                storage.savePlayer(data)
            })
        }
    }

    private fun updateFlightPermission(player: org.bukkit.entity.Player) {
        if (player.gameMode == GameMode.CREATIVE || player.gameMode == GameMode.SPECTATOR) return

        if (player.world.name == plugin.citadelWorldName) {
            player.allowFlight = true
            player.sendMessage("§b[Sistema] §fPropulsores de Vigor activados.")
        } else {
            player.isFlying = false
            player.allowFlight = false
        }
    }
    private fun updateWorldContext(player: Player) {
        player.level = 0
        player.exp = 0f
    }

    private fun startAutoSave() {
        val interval = plugin.config.getLong("storage.autosave_interval", 10)
        if (interval <= 0) return

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, Runnable {
            val count = dataMap.size
            if (count > 0) {
                for (data in dataMap.values) {
                    storage.savePlayer(data)
                }
                plugin.logger.info("Auto-guardado completado para $count jugadores.") // (Debug)
            }
        }, interval * 1200L, interval * 1200L)
    }
}