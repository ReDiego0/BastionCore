package org.ReDiego0.bastionCore.command

import org.ReDiego0.bastionCore.combat.WeaponType
import org.ReDiego0.bastionCore.utils.ItemTags
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class DebugCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

        if (args.isNotEmpty() && args[0].equals("arma", ignoreCase = true)) {
            // Uso: /bastiondebug arma KATANA
            if (args.size < 2) {
                sender.sendMessage("§cEspecifica el tipo: KATANA, GREATSWORD, SPEAR...")
                return true
            }

            try {
                val type = WeaponType.valueOf(args[1].uppercase())

                val item = ItemStack(Material.IRON_SWORD)
                val meta = item.itemMeta
                meta.setDisplayName("§6${type.displayName} de Prueba")
                item.itemMeta = meta
                val taggedItem = ItemTags.setWeaponType(item, type)

                sender.inventory.addItem(taggedItem)
                sender.sendMessage("§aRecibiste una ${type.displayName}.")

            } catch (e: IllegalArgumentException) {
                sender.sendMessage("§cTipo de arma inválido.")
            }
        }
        return true
    }
}