package org.ReDiego0.bastionCore.listener

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.inventory.MissionBoardHolder
import org.ReDiego0.bastionCore.utils.ContractUtils
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class BoardListener(private val plugin: BastionCore) : Listener {

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        if (event.inventory.holder !is MissionBoardHolder) return
        event.isCancelled = true

        val player = event.whoClicked as Player
        val currentItem = event.currentItem ?: return
        if (currentItem.type != Material.PAPER) return

        val pdc = currentItem.itemMeta.persistentDataContainer
        if (!pdc.has(ContractUtils.MISSION_TYPE_KEY, org.bukkit.persistence.PersistentDataType.STRING)) return

        if (player.inventory.firstEmpty() == -1) {
            player.sendMessage("§c¡Tu inventario está lleno!")
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f)
            return
        }

        val finalContract = plugin.boardCycleManager.takeMission(currentItem)

        player.inventory.addItem(finalContract)

        player.sendMessage("§aHas aceptado un nuevo contrato.")
        player.sendMessage("§7Recuerda: Tienes un tiempo límite para iniciarlo.")
        player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f)

        player.closeInventory()
    }
}