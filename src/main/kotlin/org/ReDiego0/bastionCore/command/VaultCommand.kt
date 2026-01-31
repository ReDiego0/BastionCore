package org.ReDiego0.bastionCore.command

import org.ReDiego0.bastionCore.BastionCore
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class VaultCommand(private val plugin: BastionCore) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

        if (sender.world.name != plugin.citadelWorldName) {
            sender.sendMessage("§cEl Baúl solo es accesible desde Bastión.")
            return true
        }

        plugin.vaultManager.openVault(sender)
        return true
    }
}