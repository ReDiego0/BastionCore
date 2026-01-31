package org.ReDiego0.bastionCore.command

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.manager.LoadoutValidator
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MissionCommand(private val plugin: BastionCore) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

        if (args.isEmpty()) {
            sender.sendMessage("§cUso: /mision iniciar <plantilla> | /mision salir")
            return true
        }

        when (args[0].lowercase()) {
            "iniciar" -> {
                if (args.size < 2) {
                    sender.sendMessage("§cDebes especificar la plantilla. Ej: /mision iniciar bosque")
                    return true
                }
                val templateName = args[1]

                if (!LoadoutValidator.canDeploy(sender)) {
                    return true
                }

                sender.sendMessage("§e[Sistema] §fPreparando zona de despliegue: $templateName...")

                val world = plugin.instanceManager.createInstance(templateName)

                if (world != null) {
                    sender.teleport(world.spawnLocation)
                    sender.sendMessage("§a[Misión] §fDespliegue exitoso. ¡Buena caza!")
                } else {
                    sender.sendMessage("§cError: No existe la plantilla '$templateName' en la carpeta /templates/")
                }
            }

            "salir" -> {
                val world = sender.world
                if (world.name.startsWith("inst_")) {
                    plugin.instanceManager.unloadInstance(world.name)
                } else {
                    sender.sendMessage("§cNo estás en una misión activa.")
                }
            }
        }
        return true
    }
}