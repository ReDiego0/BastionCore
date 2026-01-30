package org.ReDiego0.bastionCore.data

import org.ReDiego0.bastionCore.BastionCore
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PlayerDataManager(private val plugin: BastionCore) : Listener {

    private val dataMap = ConcurrentHashMap<UUID, PlayerData>()

    fun getData(uuid: UUID): PlayerData? {
        return dataMap[uuid]
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player

        val data = PlayerData(player.uniqueId, player.name)
        dataMap[player.uniqueId] = data
        player.foodLevel = 20
        player.saturation = 0f

        updateFlightPermission(player)
    }

    @EventHandler
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        updateFlightPermission(event.player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        dataMap.remove(event.player.uniqueId)
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
}