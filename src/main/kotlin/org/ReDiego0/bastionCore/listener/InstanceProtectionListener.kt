package org.ReDiego0.bastionCore.listener

import org.ReDiego0.bastionCore.BastionCore
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

class InstanceProtectionListener(private val plugin: BastionCore) : Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val worldName = player.world.name

        if (!worldName.startsWith("inst_")) return
        if (player.gameMode == GameMode.CREATIVE && player.isOp) return

        val mission = plugin.gameManager.getMission(worldName)

        if (mission == null) {
            event.isCancelled = true
            return
        }

        val blockName = event.block.type.name

        if (!mission.allowedBlocks.contains(blockName)) {
            event.isCancelled = true
            player.sendActionBar("§c¡Es indestructible!")
        }
    }

    // Redundante, TODO: Borrar luego
    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (event.player.world.name.startsWith("inst_") && !event.player.isOp) {
            event.isCancelled = true
        }
    }
}