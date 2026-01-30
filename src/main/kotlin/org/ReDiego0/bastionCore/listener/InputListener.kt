package org.ReDiego0.bastionCore.listener

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.combat.CombatManager
import org.ReDiego0.bastionCore.combat.WeaponType
import org.ReDiego0.bastionCore.utils.ItemTags
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot

class InputListener(
    private val plugin: BastionCore,
    private val combatManager: CombatManager
) : Listener {

    @EventHandler
    fun onRightClick(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.hand != EquipmentSlot.HAND) return

        val player = event.player
        val item = player.inventory.itemInMainHand

        val weaponType = ItemTags.getWeaponType(item)
        if (weaponType == WeaponType.NONE) return

        combatManager.handleRightClick(player, weaponType)
    }

    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        val itemDrop = event.itemDrop
        val itemStack = itemDrop.itemStack

        val weaponType = ItemTags.getWeaponType(itemStack)
        if (weaponType == WeaponType.NONE) return

        event.isCancelled = true
        combatManager.handleWeaponSkill(event.player, weaponType)
    }

    @EventHandler
    fun onSwapHand(event: PlayerSwapHandItemsEvent) {
        event.isCancelled = true

        val player = event.player
        val isAtCitadel = player.world.name == plugin.citadelWorldName

        if (isAtCitadel) {
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
            player.sendMessage("§a[Bastión] §fAbriendo menú principal... (Próximamente)")

        } else {
            combatManager.handleClassUltimate(player)
        }
    }
}