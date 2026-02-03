package org.ReDiego0.bastionCore.manager

import io.lumine.mythic.api.mobs.MythicMob
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity

object SpawnManager {
    fun spawnBoss(location: Location, mobId: String): Boolean {
        val chunk = location.chunk
        if (!chunk.isLoaded) {
            chunk.load()
        }

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
            val entity = location.world?.spawnEntity(location, type)

            if (entity is LivingEntity) {
                entity.removeWhenFarAway = false
            }
            true
        } catch (e: IllegalArgumentException) {
            org.bukkit.Bukkit.getLogger().warning("[BastionCore] Error: El mob '$mobId' no es válido.")
            false
        }
    }
}