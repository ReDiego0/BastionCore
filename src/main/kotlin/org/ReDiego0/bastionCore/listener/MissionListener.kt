package org.ReDiego0.bastionCore.listener

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.manager.MissionManager
import org.ReDiego0.bastionCore.utils.ContractUtils
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class MissionListener(
    private val plugin: BastionCore,
    private val missionManager: MissionManager
) : Listener {

    private val interactCooldown = HashMap<UUID, Long>()

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        val player = event.player

        val lastInteract = interactCooldown.getOrDefault(player.uniqueId, 0L)
        if (System.currentTimeMillis() - lastInteract < 1000) return
        interactCooldown[player.uniqueId] = System.currentTimeMillis()

        val item = event.item ?: return
        val block = event.clickedBlock ?: return

        if (!item.hasItemMeta()) return
        val pdc = item.itemMeta.persistentDataContainer

        if (!pdc.has(ContractUtils.MISSION_TYPE_KEY, PersistentDataType.STRING) &&
            !pdc.has(ContractUtils.MISSION_ID_KEY, PersistentDataType.STRING)) return

        if (ContractUtils.isExpired(item)) {
            player.sendMessage("§c[!] Este contrato ha expirado.")
            player.playSound(player.location, org.bukkit.Sound.ENTITY_ITEM_BREAK, 1f, 0.5f)
            player.inventory.setItemInMainHand(null)
            return
        }

        var requiredBlockName = "OAK_FENCE"
        val type = pdc.get(ContractUtils.MISSION_TYPE_KEY, PersistentDataType.STRING)

        if (type == "PROCEDURAL") {
            requiredBlockName = pdc.get(ContractUtils.DATA_TRIGGER_KEY, PersistentDataType.STRING) ?: "OAK_FENCE"
        } else {
            val missionId = pdc.get(ContractUtils.MISSION_ID_KEY, PersistentDataType.STRING)
            requiredBlockName = ContractUtils.getConfig().getString("special.$missionId.trigger_block", "CARTOGRAPHY_TABLE")!!
        }

        if (block.type.name != requiredBlockName) {
            player.sendMessage("§c[!] Lugar incorrecto.")
            player.sendMessage("§7Esta misión debe despacharse en: §e$requiredBlockName")
            return
        }

        event.isCancelled = true
        missionManager.startMission(player, item)
    }
}