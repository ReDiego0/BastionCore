package org.ReDiego0.bastionCore.manager

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object RewardManager {

    fun grantReward(player: Player, money: Double, items: List<String>, commands: List<String>) {
        player.sendMessage("§6+$money Oro")

        for (itemStr in items) {
            val parts = itemStr.split(":")
            val mat = Material.getMaterial(parts[0]) ?: continue
            val amount = if (parts.size > 1) parts[1].toInt() else 1

            player.inventory.addItem(ItemStack(mat, amount))
            player.sendMessage("§a+Recibido: $amount x ${mat.name}")
        }

        val console = Bukkit.getConsoleSender()
        for (cmd in commands) {
            val finalCmd = cmd.replace("%player%", player.name)
            Bukkit.dispatchCommand(console, finalCmd)
        }
    }
}