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
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.EquipmentSlot

class InputListener(
    private val plugin: BastionCore,
    private val combatManager: CombatManager
) : Listener {

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        if (!player.world.name.startsWith("inst_")) return
        if (event.hand != EquipmentSlot.HAND) return

        val item = player.inventory.itemInMainHand
        val weaponType = ItemTags.getWeaponType(item)

        if (weaponType == WeaponType.NONE) return

        if (weaponType == WeaponType.YUMI) {
            if (event.action == Action.LEFT_CLICK_AIR || event.action == Action.LEFT_CLICK_BLOCK) {
                event.isCancelled = true
                combatManager.handleRightClick(player, weaponType)
            }
            return
        }

        if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
            combatManager.handleRightClick(player, weaponType)
        }
    }

    @EventHandler
    fun onSneak(event: PlayerToggleSneakEvent) {
        val player = event.player
        if (!event.isSneaking) return
        if (!player.world.name.startsWith("inst_")) return
        if (player.world.name == plugin.citadelWorldName) return
        combatManager.handleDirectionalDash(player)
    }



    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        val player = event.player
        if (!player.world.name.startsWith("inst_")) return

        val itemStack = event.itemDrop.itemStack
        val weaponType = ItemTags.getWeaponType(itemStack)

        if (weaponType == WeaponType.NONE) return

        event.isCancelled = true
        if (player.world.name == plugin.citadelWorldName) return

        combatManager.handleWeaponPrimary(player, weaponType)
    }

    @EventHandler
    fun onSwapHand(event: PlayerSwapHandItemsEvent) {
        event.isCancelled = true

        val player = event.player
        val isAtCitadel = player.world.name == plugin.citadelWorldName

        if (isAtCitadel) {
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
            player.performCommand("menu")
        } else {
            if (player.world.name.startsWith("inst_")) {
                combatManager.handleClassUltimate(player)
            }
        }
    }
}