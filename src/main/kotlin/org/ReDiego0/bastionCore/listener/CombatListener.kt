package org.ReDiego0.bastionCore.listener

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.combat.WeaponType
import org.ReDiego0.bastionCore.utils.ItemTags
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent

class CombatListener(private val plugin: BastionCore) : Listener {

    @EventHandler
    fun onAttack(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player) return
        if (event.entity !is LivingEntity) return

        val player = event.damager as Player
        val data = plugin.playerDataManager.getData(player.uniqueId) ?: return

        val gainArmed = plugin.config.getDouble("combat.damage_ultimate_gain", 2.0)
        val gainUnarmed = plugin.config.getDouble("combat.damage_ultimate_gain_unarmed", 1.0)

        val weaponType = ItemTags.getWeaponType(player.inventory.itemInMainHand)
        val chargeGain = if (weaponType != WeaponType.NONE) gainArmed else gainUnarmed

        data.addCharge(player, chargeGain)
    }

    @EventHandler
    fun onKill(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return

        val data = plugin.playerDataManager.getData(killer.uniqueId) ?: return

        val gainOnKill = plugin.config.getDouble("combat.kill_ultimate_gain", 10.0)

        data.addCharge(killer, gainOnKill)
    }
}