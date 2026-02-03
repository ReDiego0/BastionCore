package org.ReDiego0.bastionCore.command

import org.ReDiego0.bastionCore.BastionCore
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PartyCommand(private val plugin: BastionCore) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }

        when (args[0].lowercase()) {
            "crear", "create" -> plugin.partyManager.createParty(sender)
            "invitar", "invite" -> {
                if (args.size < 2) {
                    sender.sendMessage("§cUso: /party invite <jugador>")
                    return true
                }
                val target = Bukkit.getPlayer(args[1])
                if (target == null) {
                    sender.sendMessage("§cJugador no encontrado.")
                    return true
                }
                plugin.partyManager.sendInvite(sender, target)
            }
            "aceptar", "accept" -> plugin.partyManager.acceptInvite(sender)
            "salir", "leave" -> plugin.partyManager.leaveParty(sender)
            "info" -> showInfo(sender)
            else -> sendHelp(sender)
        }
        return true
    }

    private fun showInfo(player: Player) {
        val party = plugin.partyManager.getParty(player.uniqueId)
        if (party == null) {
            player.sendMessage("§cNo estás en una escuadra.")
            return
        }

        player.sendMessage("§8§m------------------")
        player.sendMessage("§6Escuadra de Caza:")
        val leaderName = Bukkit.getPlayer(party.leaderId)?.name ?: "Desconocido"
        player.sendMessage("§7Líder: §e$leaderName")
        player.sendMessage("§7Miembros (${party.members.size}/${party.maxSize}):")

        for (uuid in party.members) {
            val p = Bukkit.getPlayer(uuid)
            val status = if (p != null) "§aOnline" else "§cOffline"
            val name = p?.name ?: "..."
            player.sendMessage(" - §f$name §7($status)")
        }
        player.sendMessage("§8§m------------------")
    }

    private fun sendHelp(player: Player) {
        player.sendMessage("§6Comandos de Escuadra:")
        player.sendMessage("§f/party create §7- Crear escuadra")
        player.sendMessage("§f/party invite <jugador> §7- Invitar amigo")
        player.sendMessage("§f/party accept §7- Aceptar invitación")
        player.sendMessage("§f/party leave §7- Salir")
        player.sendMessage("§f/party info §7- Ver miembros")
    }
}