package org.ReDiego0.bastionCore.manager

import dev.lone.itemsadder.api.CustomStack
import org.ReDiego0.bastionCore.BastionCore
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.persistence.PersistentDataType
import java.util.*

class VaultManager(private val plugin: BastionCore) {
    private val vaults = HashMap<UUID, Inventory>()
    private val kitKey = NamespacedKey(plugin, "starter_kit_received")

    fun openVault(player: Player) {
        val inv = getVault(player)
        player.openInventory(inv)
    }

    private fun getVault(player: Player): Inventory {
        val uuid = player.uniqueId

        if (!vaults.containsKey(uuid)) {
            val newVault = Bukkit.createInventory(null, 54, "§8Baúl Personal")

            if (!hasReceivedKit(player)) {
                addStarterItems(newVault)
                markKitAsReceived(player)
                plugin.logger.info("Kit de inicio entregado a ${player.name}")
            }

            vaults[uuid] = newVault
        }
        return vaults[uuid]!!
    }

    private fun addStarterItems(inv: Inventory) {
        val starterWeapons = listOf(
            "bastion:starter_greatsword",
            "bastion:starter_hammer",
            "bastion:starter_spear",
            "bastion:starter_katana",
            "bastion:starter_dagger",
            "bastion:starter_bow"
        )

        for (id in starterWeapons) {
            val customStack = CustomStack.getInstance(id)
            if (customStack != null) {
                inv.addItem(customStack.itemStack)
            } else {
                plugin.logger.warning("Error: No se encontró el arma inicial '$id' en ItemsAdder.")
            }
        }
    }

    private fun hasReceivedKit(player: Player): Boolean {
        return player.persistentDataContainer.has(kitKey, PersistentDataType.BYTE)
    }

    private fun markKitAsReceived(player: Player) {
        player.persistentDataContainer.set(kitKey, PersistentDataType.BYTE, 1.toByte())
    }
}