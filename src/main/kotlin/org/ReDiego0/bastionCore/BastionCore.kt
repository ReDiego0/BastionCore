package org.ReDiego0.bastionCore

import org.ReDiego0.bastionCore.data.PlayerDataManager
import org.ReDiego0.bastionCore.listener.StaminaListener
import org.ReDiego0.bastionCore.task.StaminaTask
import org.bukkit.plugin.java.JavaPlugin

class BastionCore : JavaPlugin() {

    companion object {
        lateinit var instance: BastionCore
            private set
    }

    lateinit var playerDataManager: PlayerDataManager

    var citadelWorldName: String = "Bastion"

    override fun onEnable() {
        instance = this
        saveDefaultConfig()
        citadelWorldName = config.getString("citadel_world", "Bastion")!!

        playerDataManager = PlayerDataManager(this)

        server.pluginManager.registerEvents(playerDataManager, this)
        server.pluginManager.registerEvents(StaminaListener(), this)

        StaminaTask(this).runTaskTimer(this, 20L, 5L)

        logger.info("§a[BastionCore] Sistemas de soporte vital activos. Ciudadela: $citadelWorldName")
    }

    override fun onDisable() {
        logger.info("§c[BastionCore] Cerrando conexión con el servidor central...")
    }
}