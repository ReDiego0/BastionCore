package org.ReDiego0.bastionCore.manager

import org.ReDiego0.bastionCore.combat.WeaponType
import org.ReDiego0.bastionCore.utils.ItemTags
import org.bukkit.entity.Player

object LoadoutValidator {
    fun canDeploy(player: Player): Boolean {
        val inventory = player.inventory
        var weaponCount = 0
        for (item in inventory.contents) {
            if (item == null) continue

            val type = ItemTags.getWeaponType(item)
            if (type != WeaponType.NONE) {
                weaponCount++
            }
        }

        if (weaponCount > 1) {
            player.sendMessage("§c[!] Protocolo de Misión Denegado")
            player.sendMessage("§7Estás cargando con §c$weaponCount armas§7.")
            player.sendMessage("§eRegla: Solo se permite 1 arma por contrato.")
            player.sendMessage("§f>> Guarda las armas extra en el /baul.")
            return false
        }

        val emptySlots = inventory.contents.count { it == null }
        if (emptySlots < 5) {
            player.sendMessage("§e[!] Advertencia: Tu mochila está casi llena. Tendrás poco espacio para materiales.")
        }

        return true
    }
}