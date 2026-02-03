package org.ReDiego0.bastionCore.command

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.combat.Role
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ClassCommand(private val plugin: BastionCore) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

        if (args.isEmpty()) {
            sender.sendMessage("§cUso: /clase elegir <ROL>")
            return true
        }

        if (args[0].equals("elegir", ignoreCase = true) || args[0].equals("set", ignoreCase = true)) {
            if (args.size < 2) {
                sender.sendMessage("§cEspecifica el rol: VANGUARDIA, CAZADOR, ASALTANTE...")
                return true
            }

            val roleName = args[1].uppercase()
            try {
                val newRole = Role.valueOf(roleName)
                val data = plugin.playerDataManager.getData(sender.uniqueId) ?: return true

                data.currentRole = newRole
                data.ultimateCharge = 0.0
                data.syncVanillaExp()

                sender.sendMessage("§8§m--------------------------------")
                sender.sendMessage("§a§lCLASE ACTUALIZADA (DEBUG)")
                sender.sendMessage("§7Ahora eres: §e${newRole.displayName}")
                sender.sendMessage("§8§m--------------------------------")

                sender.playSound(sender.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)

            } catch (e: IllegalArgumentException) {
                sender.sendMessage("§cRol inválido. Opciones: ${Role.entries.joinToString(", ")}")
            }
        }
        return true
    }
}