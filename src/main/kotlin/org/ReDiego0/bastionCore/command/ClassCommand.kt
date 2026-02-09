package org.ReDiego0.bastionCore.command

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.combat.Role
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ClassCommand(private val plugin: BastionCore) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // /clase set <Jugador> <Rol>
        if (args.size < 3 || !args[0].equals("set", ignoreCase = true)) {
            sender.sendMessage("§cUso correcto: /clase set <Jugador> <Rol>")
            return true
        }

        val targetName = args[1]
        val targetPlayer = Bukkit.getPlayer(targetName)

        if (targetPlayer == null || !targetPlayer.isOnline) {
            sender.sendMessage("§cError: El jugador '$targetName' no está en línea.")
            return true
        }

        val roleName = args[2].uppercase()

        try {
            val newRole = Role.valueOf(roleName)
            val data = plugin.playerDataManager.getData(targetPlayer.uniqueId)

            if (data == null) {
                sender.sendMessage("§cError: No se pudieron cargar los datos de ${targetPlayer.name}.")
                return true
            }

            data.currentRole = newRole
            data.ultimateCharge = 0.0

            targetPlayer.sendMessage(" ")
            targetPlayer.sendMessage("§8§m---------------------------------------")
            targetPlayer.sendMessage("   §6§l⚔ NUEVA CLASE ASIGNADA ⚔")
            targetPlayer.sendMessage(" ")
            targetPlayer.sendMessage("   §7Has cambiado tu especialización a:")
            targetPlayer.sendMessage("   §e§l${newRole.displayName.uppercase()}")
            targetPlayer.sendMessage(" ")
            targetPlayer.sendMessage("   §7Tus habilidades se han reiniciado.")
            targetPlayer.sendMessage("§8§m---------------------------------------")
            targetPlayer.sendMessage(" ")

            targetPlayer.playSound(targetPlayer.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)

            sender.sendMessage("§a[✔] Clase de §f${targetPlayer.name} §aactualizada a §f${newRole.name}§a.")

        } catch (e: IllegalArgumentException) {
            val validRoles = Role.entries.joinToString(", ") { it.name }
            sender.sendMessage("§cRol inválido '$roleName'. Opciones: §f$validRoles")
        }

        return true
    }
}