package org.ReDiego0.bastionCore.manager

import io.lumine.mythic.api.mobs.MythicMob
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import org.bukkit.Location
import org.bukkit.entity.EntityType

object SpawnManager {
    fun spawnBoss(location: Location, mobId: String): Boolean {

        if (org.bukkit.Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
            val mobManager = MythicBukkit.inst().mobManager

            val mythicMob: MythicMob? = mobManager.getMythicMob(mobId).orElse(null)

            if (mythicMob != null) {
                val abstractLocation = BukkitAdapter.adapt(location)
                mythicMob.spawn(abstractLocation, 1.0)
                return true
            }
        }

        return try {
            val type = EntityType.valueOf(mobId.uppercase())
            location.world?.spawnEntity(location, type)
            true
        } catch (e: IllegalArgumentException) {
            org.bukkit.Bukkit.getLogger().warning("[BastionCore] Error: El mob '$mobId' no existe en MythicMobs ni en Vanilla.")
            false
        }
    }
}