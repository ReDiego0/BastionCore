package org.ReDiego0.bastionCore

import org.ReDiego0.bastionCore.combat.CombatManager
import org.ReDiego0.bastionCore.data.PlayerDataManager
import org.ReDiego0.bastionCore.listener.CitadelListener
import org.ReDiego0.bastionCore.listener.CombatListener
import org.ReDiego0.bastionCore.listener.GameListener
import org.ReDiego0.bastionCore.listener.InputListener
import org.ReDiego0.bastionCore.listener.MissionListener
import org.ReDiego0.bastionCore.listener.StaminaListener
import org.ReDiego0.bastionCore.listener.UltimateListener
import org.ReDiego0.bastionCore.manager.BoardCycleManager
import org.ReDiego0.bastionCore.manager.CooldownManager
import org.ReDiego0.bastionCore.manager.GameManager
import org.ReDiego0.bastionCore.manager.InstanceManager
import org.ReDiego0.bastionCore.manager.MissionManager
import org.ReDiego0.bastionCore.manager.VaultManager
import org.ReDiego0.bastionCore.task.StaminaTask
import org.ReDiego0.bastionCore.utils.ContractUtils
import org.bukkit.plugin.java.JavaPlugin

class BastionCore : JavaPlugin() {

    companion object {
        lateinit var instance: BastionCore
            private set
    }

    lateinit var playerDataManager: PlayerDataManager
    lateinit var combatManager: CombatManager
    lateinit var cooldownManager: CooldownManager
    lateinit var vaultManager: VaultManager
    lateinit var instanceManager: InstanceManager
    lateinit var boardCycleManager: BoardCycleManager
    lateinit var missionManager: MissionManager
    lateinit var gameManager: GameManager

    var citadelWorldName: String = "Bastion"

    override fun onEnable() {
        instance = this
        saveDefaultConfig()
        ContractUtils.reloadMissions()
        citadelWorldName = config.getString("citadel_world", "Bastion")!!

        playerDataManager = PlayerDataManager(this)
        cooldownManager = CooldownManager()
        combatManager = CombatManager(this)
        vaultManager = VaultManager()
        instanceManager = InstanceManager(this)
        boardCycleManager = BoardCycleManager(this)
        missionManager = MissionManager(this)
        gameManager = GameManager(this)


        server.pluginManager.registerEvents(playerDataManager, this)
        server.pluginManager.registerEvents(StaminaListener(), this)
        server.pluginManager.registerEvents(CitadelListener(this), this)
        server.pluginManager.registerEvents(InputListener(this, combatManager), this)
        server.pluginManager.registerEvents(UltimateListener(),this)
        server.pluginManager.registerEvents(CombatListener(this), this)
        server.pluginManager.registerEvents(MissionListener(this, missionManager), this)
        server.pluginManager.registerEvents(org.ReDiego0.bastionCore.listener.BoardListener(this), this)
        server.pluginManager.registerEvents(GameListener(this), this)

        getCommand("bastiondebug")?.setExecutor(org.ReDiego0.bastionCore.command.DebugCommand())
        getCommand("baul")?.setExecutor(org.ReDiego0.bastionCore.command.VaultCommand(this))
        getCommand("mision")?.setExecutor(org.ReDiego0.bastionCore.command.MissionCommand(this))
        getCommand("tablon")?.setExecutor(org.ReDiego0.bastionCore.command.BoardCommand(this))

        StaminaTask(this).runTaskTimer(this, 20L, 5L)

        logger.info("§a[BastionCore] Sistemas de soporte vital activos. Ciudadela: $citadelWorldName")

        if (server.pluginManager.getPlugin("PlaceholderAPI") != null) {
            org.ReDiego0.bastionCore.hooks.BastionExpansion(this).register()
            logger.info("[BastionCore] PlaceholderAPI detectado y conectado.")
        }
    }

    override fun onDisable() {
        instanceManager.cleanupAll()
        logger.info("§c[BastionCore] Cerrando conexión con el servidor central...")
    }
}