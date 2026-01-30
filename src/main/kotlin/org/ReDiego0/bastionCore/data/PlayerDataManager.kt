package org.ReDiego0.bastionCore.data

import org.ReDiego0.bastionCore.BastionCore
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
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

        player.sendMessage("§6[Bastion] §fIdentidad confirmada. Bienvenido, Contratista.")
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        dataMap.remove(event.player.uniqueId)
    }
}