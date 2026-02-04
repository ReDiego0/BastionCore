package org.ReDiego0.bastionCore.command

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.combat.WeaponType
import org.ReDiego0.bastionCore.utils.ItemTags
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class DebugCommand(private val plugin: BastionCore) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }

        when (args[0].lowercase()) {

            "arma" -> {
                if (args.size < 2) {
                    sender.sendMessage("§cUso: /bastiondebug arma <TIPO>")
                    return true
                }
                try {
                    val type = WeaponType.valueOf(args[1].uppercase())

                    val material = when (type) {
                        WeaponType.GREATSWORD -> Material.NETHERITE_SWORD
                        WeaponType.HAMMER -> Material.MACE
                        WeaponType.SPEAR -> Material.DIAMOND_SWORD
                        WeaponType.BOW -> Material.BOW
                        WeaponType.KATANA -> Material.IRON_SWORD
                        WeaponType.DUAL_BLADES -> Material.GOLDEN_SWORD
                        else -> Material.WOODEN_SWORD
                    }

                    val item = ItemStack(material)
                    val meta = item.itemMeta
                    meta.setDisplayName("§6${type.displayName} §7(Debug)")
                    meta.lore = listOf("§7Item de prueba del desarrollador.")
                    item.itemMeta = meta

                    val taggedItem = ItemTags.setWeaponType(item, type)

                    sender.inventory.addItem(taggedItem)
                    sender.playSound(sender.location, Sound.ENTITY_ITEM_PICKUP, 1f, 1f)
                    sender.sendMessage("§a[Debug] §fRecibiste: ${type.displayName}")

                } catch (e: IllegalArgumentException) {
                    sender.sendMessage("§cTipos válidos: ${WeaponType.entries.joinToString(", ")}")
                }
            }

            "resetrank" -> {
                val data = plugin.playerDataManager.getData(sender.uniqueId) ?: return true
                data.hunterRank = 1
                data.guildPoints = 0
                data.syncVanillaExp()
                sender.sendMessage("§a[Debug] §fTu Rango de Cazador ha sido reiniciado a 1.")
                sender.playSound(sender.location, Sound.BLOCK_ANVIL_BREAK, 1f, 1f)
            }

            "maxult" -> {
                val data = plugin.playerDataManager.getData(sender.uniqueId) ?: return true
                data.ultimateCharge = 100.0
                data.syncVanillaExp()
                sender.sendMessage("§b[Debug] §fBarra de Ultimate cargada al 100%.")
                sender.playSound(sender.location, Sound.BLOCK_BEACON_POWER_SELECT, 1f, 2f)
            }

            "addxp" -> {
                if (args.size < 2) {
                    sender.sendMessage("§cUso: /bastiondebug addxp <cantidad>")
                    return true
                }
                val amount = args[1].toIntOrNull() ?: 10
                val data = plugin.playerDataManager.getData(sender.uniqueId)

                sender.sendMessage("§e[Debug] §fAñadiendo $amount XP...")
                data?.addGuildPoints(amount)
            }

            else -> sendHelp(sender)
        }
        return true
    }

    private fun sendHelp(p: Player) {
        p.sendMessage("§6--- Bastion Debug ---")
        p.sendMessage("§f/bastiondebug arma <tipo> §7- Conseguir arma")
        p.sendMessage("§f/bastiondebug resetrank §7- Volver a Rango 1")
        p.sendMessage("§f/bastiondebug maxult §7- Cargar Definitiva")
        p.sendMessage("§f/bastiondebug addxp <cant> §7- Ganar XP de gremio")
    }
}