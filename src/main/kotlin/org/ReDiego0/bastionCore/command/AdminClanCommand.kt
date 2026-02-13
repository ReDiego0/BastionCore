package org.ReDiego0.bastionCore.command

import org.ReDiego0.bastionCore.BastionCore
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AdminClanCommand(private val plugin: BastionCore) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Solo jugadores.")
            return true
        }

        if (!sender.hasPermission("bastion.admin.clan")) {
            sender.sendMessage("§cSin permiso.")
            return true
        }

        if (args.isNotEmpty() && args[0].equals("crear", ignoreCase = true)) {
            if (args.size < 4) {
                sender.sendMessage("§cUso: /adminclan crear <Jugador> <ID_Unico> <Nombre Visible>")
                return true
            }

            val targetName = args[1]
            val clanId = args[2]
            val displayName = args.drop(3).joinToString(" ")

            val targetPlayer = Bukkit.getPlayer(targetName)
            if (targetPlayer == null) {
                sender.sendMessage("§cEl jugador $targetName debe estar online para cobrarle.")
                return true
            }

            plugin.clanManager.adminCreateClan(sender, targetPlayer, clanId, displayName)
            return true
        }

        sender.sendMessage("§cUso: /adminclan crear <Jugador> <ID> <Nombre>")
        return true
    }
}