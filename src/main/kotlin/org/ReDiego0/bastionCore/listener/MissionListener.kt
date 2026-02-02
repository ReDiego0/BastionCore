package org.ReDiego0.bastionCore.listener

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.manager.MissionManager
import org.ReDiego0.bastionCore.utils.ContractUtils
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class MissionListener(
    private val plugin: BastionCore,
    private val missionManager: MissionManager
) : Listener {

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.hand != EquipmentSlot.HAND) return

        val item = event.item ?: return
        val block = event.clickedBlock ?: return

        val missionId = ContractUtils.getMissionId(item) ?: return

        val config = ContractUtils.getConfig()
        val type = config.getString("missions.$missionId.type", "CONTRACT")

        var isValidLocation = false

        if (type == "STORY") {
            // Especiales
            if (block.type == Material.CARTOGRAPHY_TABLE) {
                isValidLocation = true
            } else {
                event.player.sendMessage("§c[!] Los Expedientes Clasificados deben procesarse en la Sala de Operaciones (Mesa de Cartografía).")
            }
        } else {
            // Misiones normales
            if (block.type.name.contains("FENCE") || block.type == Material.LECTERN) {
                isValidLocation = true
            } else {
                event.player.sendMessage("§c[!] Los Contratos de Caza se validan en el Muelle (Vallas/Atril).")
            }
        }

        if (isValidLocation) {
            event.isCancelled = true // Evitar que abra el bloque si es interactuable
            missionManager.startMission(event.player, missionId)
        }
    }
}