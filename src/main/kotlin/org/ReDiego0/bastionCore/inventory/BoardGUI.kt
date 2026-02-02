package org.ReDiego0.bastionCore.inventory

import org.ReDiego0.bastionCore.BastionCore
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BoardGUI(private val plugin: BastionCore) {

    fun openBoard(player: Player) {
        val holder = MissionBoardHolder()
        val inv = Bukkit.createInventory(holder, 45, "§8Tablón de Contratos")

        val filler = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        val meta = filler.itemMeta
        meta.setDisplayName(" ")
        filler.itemMeta = meta

        for (i in 0..8) inv.setItem(i, filler)
        for (i in 36..44) inv.setItem(i, filler)

        val clock = ItemStack(Material.CLOCK)
        val clockMeta = clock.itemMeta
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
        clock.itemMeta = clockMeta
        inv.setItem(4, clock)

        val missions = plugin.boardCycleManager.getAvailableMissions()

        for ((index, missionItem) in missions.withIndex()) {
            if (index >= 27) break
            inv.setItem(9 + index, missionItem)
        }

        player.openInventory(inv)
    }
}