package org.ReDiego0.bastionCore.listener

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.combat.WeaponType
import org.ReDiego0.bastionCore.utils.ItemTags
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class CombatListener(private val plugin: BastionCore) : Listener {

    @EventHandler
    fun onAttack(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player) return
        val player = event.damager as Player
        if (event.entity !is LivingEntity) return

        val data = plugin.playerDataManager.getData(player.uniqueId) ?: return
        var chargeGain = 3.0

        val weaponType = ItemTags.getWeaponType(player.inventory.itemInMainHand)
        if (weaponType != WeaponType.NONE) {
            chargeGain = 5.0
        }

        data.addCharge(player, chargeGain)
    }
}