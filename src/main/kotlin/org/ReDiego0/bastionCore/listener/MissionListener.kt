package org.ReDiego0.bastionCore.listener

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.manager.MissionManager
import org.ReDiego0.bastionCore.utils.ContractUtils
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

class MissionListener(private val plugin: BastionCore, private val missionManager: MissionManager) : Listener {

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val item = event.item ?: return
        val block = event.clickedBlock ?: return

        if (!item.hasItemMeta()) return
        val pdc = item.itemMeta.persistentDataContainer

        if (!pdc.has(ContractUtils.MISSION_TYPE_KEY, PersistentDataType.STRING)) return

        if (ContractUtils.isExpired(item)) {
            event.player.sendMessage("§c[!] Este contrato ha expirado.")
            event.player.playSound(event.player.location, org.bukkit.Sound.ENTITY_ITEM_BREAK, 1f, 0.5f)

            event.player.inventory.setItemInMainHand(null)
            return
        }

        var requiredBlockName = "OAK_FENCE"
        val type = pdc.get(ContractUtils.MISSION_TYPE_KEY, PersistentDataType.STRING)

        if (type == "PROCEDURAL") {
            requiredBlockName = pdc.get(ContractUtils.DATA_TRIGGER_KEY, PersistentDataType.STRING) ?: "OAK_FENCE"
        } else {
            val missionId = pdc.get(ContractUtils.MISSION_ID_KEY, PersistentDataType.STRING)
            requiredBlockName = plugin.config.getString("special.$missionId.trigger_block", "CARTOGRAPHY_TABLE")!!
        }

        if (block.type.name != requiredBlockName) {
            event.player.sendMessage("§c[!] Lugar incorrecto.")
            event.player.sendMessage("§7Esta misión debe despacharse en: §e$requiredBlockName")
            return
        }

        event.isCancelled = true
        missionManager.startMission(event.player, item)
    }
}