package org.ReDiego0.bastionCore.manager

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class VaultManager {
    private val vaults = ConcurrentHashMap<UUID, Inventory>()

    fun openVault(player: Player) {
        val vault = vaults.computeIfAbsent(player.uniqueId) {
            Bukkit.createInventory(null, 54, "§8Baúl de Equipamiento")
        }
        player.openInventory(vault)
        player.playSound(player.location, org.bukkit.Sound.BLOCK_CHEST_OPEN, 1f, 1f)
    }

    // guardar datos (onDisable)
    fun saveAll() {
        // Aquí iría la lógica de serialización a YAML/SQL
    }
}