package org.ReDiego0.bastionCore.command

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.data.Faction
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class FactionCommand(private val plugin: BastionCore) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) return false

        // COMANDO: /faccion interactuar <FACCIÓN> <JUGADOR>
        if (args[0].equals("interactuar", ignoreCase = true)) {
            if (args.size < 3) return false

            val npcFaction = Faction.fromId(args[1])
            if (npcFaction == Faction.NONE) {
                sender.sendMessage("§cFacción inválida.")
                return true
            }

            val target = Bukkit.getPlayer(args[2]) ?: return true
            val data = plugin.playerDataManager.getData(target.uniqueId) ?: return true

            if (data.faction == npcFaction) {
                plugin.factionGUI.openFactionHub(target)
            } else {
                plugin.factionGUI.openJoinMenu(target, npcFaction)
            }
            return true
        }

        if (args[0].equals("menu", ignoreCase = true)) {
            val player = sender as? Player ?: return true
            val data = plugin.playerDataManager.getData(player.uniqueId) ?: return true

            if (data.faction == Faction.NONE) {
                player.sendMessage("§cNo perteneces a ninguna facción.")
                player.sendMessage("§7Habla con los representantes en la Ciudadela para unirte.")
                player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
            } else {
                plugin.factionGUI.openFactionHub(player)
            }
            return true
        }

        return true
    }
}