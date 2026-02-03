package org.ReDiego0.bastionCore.manager

import org.ReDiego0.bastionCore.BastionCore
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.util.UUID

class VaultManager(private val plugin: BastionCore) {
    private val vaults = HashMap<UUID, Inventory>()

    fun openVault(player: Player) {
        val inv = getVault(player.uniqueId)
        player.openInventory(inv)
    }

    private fun getVault(uuid: UUID): Inventory {
        if (!vaults.containsKey(uuid)) {
            val newVault = Bukkit.createInventory(null, 27, "§8Baúl Personal")
            vaults[uuid] = newVault
        }
        return vaults[uuid]!!
    }
}