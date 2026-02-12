package org.ReDiego0.bastionCore.inventory

import dev.lone.itemsadder.api.CustomStack
import org.ReDiego0.bastionCore.BastionCore
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BoardGUI(private val plugin: BastionCore) {

    fun openBoard(player: Player) {
        val holder = MissionBoardHolder()
        val inv = Bukkit.createInventory(holder, 45, ":offset_-48::tablon_misiones:")
        val customStack = CustomStack.getInstance("gui:invisible")

        val clockItem = if (customStack != null) {
            customStack.itemStack.clone()
        } else {
            plugin.logger.warning("No se encontró 'gui:invisible' en ItemsAdder.")
            ItemStack(Material.CLOCK)
        }

        val clockMeta = clockItem.itemMeta
        val secondsLeft = plugin.boardCycleManager.getTimeRemainingSeconds()
        val minutes = secondsLeft / 60

        clockMeta.setDisplayName("§eCiclo de Misiones")
        clockMeta.lore = listOf(
            "§7Nuevos contratos en:",
            "§b${minutes} minutos",
            "",
            "§7Los contratos se renuevan",
            "§7periódicamente."
        )
        clockItem.itemMeta = clockMeta

        inv.setItem(4, clockItem)

        val missions = plugin.boardCycleManager.getAvailableMissions()

        for ((index, missionItem) in missions.withIndex()) {
            if (index >= 27) break
            inv.setItem(9 + index, missionItem)
        }

        player.openInventory(inv)
    }
}