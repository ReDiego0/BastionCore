package org.ReDiego0.bastionCore

import org.ReDiego0.bastionCore.combat.CombatManager
import org.ReDiego0.bastionCore.data.PlayerDataManager
import org.ReDiego0.bastionCore.listener.CitadelListener
import org.ReDiego0.bastionCore.listener.InputListener
import org.ReDiego0.bastionCore.listener.StaminaListener
import org.ReDiego0.bastionCore.manager.CooldownManager
import org.ReDiego0.bastionCore.task.StaminaTask
import org.bukkit.plugin.java.JavaPlugin

class BastionCore : JavaPlugin() {

    companion object {
        lateinit var instance: BastionCore
            private set
    }

    lateinit var playerDataManager: PlayerDataManager
    lateinit var combatManager: CombatManager
    lateinit var cooldownManager: CooldownManager

    var citadelWorldName: String = "Bastion"

    override fun onEnable() {
        instance = this
        saveDefaultConfig()
        citadelWorldName = config.getString("citadel_world", "Bastion")!!

        playerDataManager = PlayerDataManager(this)
        cooldownManager = CooldownManager()
        combatManager = CombatManager(this)


        server.pluginManager.registerEvents(playerDataManager, this)
        server.pluginManager.registerEvents(StaminaListener(), this)
        server.pluginManager.registerEvents(CitadelListener(this), this)
        server.pluginManager.registerEvents(InputListener(this, combatManager), this)

        getCommand("bastiondebug")?.setExecutor(org.ReDiego0.bastionCore.command.DebugCommand())

        StaminaTask(this).runTaskTimer(this, 20L, 5L)

        logger.info("§a[BastionCore] Sistemas de soporte vital activos. Ciudadela: $citadelWorldName")
    }

    override fun onDisable() {
        logger.info("§c[BastionCore] Cerrando conexión con el servidor central...")
    }
}