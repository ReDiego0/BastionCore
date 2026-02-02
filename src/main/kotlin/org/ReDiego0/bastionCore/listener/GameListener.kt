package org.ReDiego0.bastionCore.listener

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.combat.MissionType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityPickupItemEvent

class GameListener(private val plugin: BastionCore) : Listener {

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity
        val world = entity.world

        if (!world.name.startsWith("inst_")) return
        val mission = plugin.gameManager.getMission(world.name) ?: return

        if (mission.type == MissionType.GATHER) return

        var isTarget = false

        if (entity.type.name.equals(mission.targetId, ignoreCase = true)) {
            isTarget = true
        }

        if (!isTarget && org.bukkit.Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
            val mobManager = io.lumine.mythic.bukkit.MythicBukkit.inst().mobManager
            if (mobManager.isActiveMob(entity.uniqueId)) {
                val mmInstance = mobManager.getMythicMobInstance(entity)
                if (mmInstance.type.internalName.equals(mission.targetId, ignoreCase = true)) {
                    isTarget = true
                }
            }
        }

        if (isTarget) {
            plugin.gameManager.addProgress(world.name, 1)
        }
    }

    @EventHandler
    fun onPickup(event: EntityPickupItemEvent) {
        val player = event.entity as? Player ?: return
        val world = player.world
        if (!world.name.startsWith("inst_")) return

        val mission = plugin.gameManager.getMission(world.name) ?: return
        if (mission.type != MissionType.GATHER) return

        val item = event.item.itemStack
        if (item.type.name.equals(mission.targetId, ignoreCase = true)) {
            plugin.gameManager.addProgress(world.name, item.amount)
            player.playSound(player.location, org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 2f)
        }
    }
}