package org.ReDiego0.bastionCore.listener

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.combat.WeaponType
import org.ReDiego0.bastionCore.utils.ItemTags
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerJoinEvent

class CitadelListener(private val plugin: BastionCore) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        if (player.world.name == plugin.citadelWorldName) {
            player.gameMode = GameMode.ADVENTURE
            player.allowFlight = false
            plugin.playerDataManager.getData(player.uniqueId)?.syncVanillaExp()
        }
    }

    @EventHandler
    fun onChangeWorld(event: PlayerChangedWorldEvent) {
        val player = event.player
        if (player.world.name == plugin.citadelWorldName) {
            plugin.playerDataManager.getData(player.uniqueId)?.syncVanillaExp()
            player.gameMode = GameMode.ADVENTURE
            player.allowFlight = false
        }
        else {
            player.exp = 0f
            player.level = 0
        }
    }

    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        val player = event.player
        if (player.world.name != plugin.citadelWorldName) return

        val item = event.itemDrop.itemStack
        val typeName = item.type.name

        val weaponType = ItemTags.getWeaponType(item)
        val isRPGWeapon = weaponType != WeaponType.NONE

        val isArmorOrGear = typeName.endsWith("_HELMET") ||
                typeName.endsWith("_CHESTPLATE") ||
                typeName.endsWith("_LEGGINGS") ||
                typeName.endsWith("_BOOTS") ||
                typeName == "SHIELD" ||
                typeName == "ELYTRA" ||
                typeName == "ARROW"

        if (isRPGWeapon || isArmorOrGear) {
            event.isCancelled = true
            player.sendMessage("§c[!] No puedes tirar tu equipamiento.")
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f)
        }
    }

    // Un poco innecesario, TODO: Borrar luego
    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        if (event.entity !is Player) return
        val player = event.entity as Player

        if (event.cause == EntityDamageEvent.DamageCause.FALL) {
            if (player.world.name == plugin.citadelWorldName) {
                event.isCancelled = true
            }
        }
    }
}