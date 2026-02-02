package org.ReDiego0.bastionCore.command

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.inventory.BoardGUI
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class BoardCommand(private val plugin: BastionCore) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

        if (sender.world.name != plugin.citadelWorldName) {
            sender.sendMessage("§cSolo puedes acceder al tablón en la Ciudadela.")
            return true
        }

        BoardGUI(plugin).openBoard(sender)
        return true
    }
}