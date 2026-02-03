package org.ReDiego0.bastionCore

import net.milkbowl.vault.economy.Economy
import org.ReDiego0.bastionCore.combat.CombatManager
import org.ReDiego0.bastionCore.data.PlayerDataManager
import org.ReDiego0.bastionCore.listener.*
import org.ReDiego0.bastionCore.manager.*
import org.ReDiego0.bastionCore.task.StaminaTask
import org.ReDiego0.bastionCore.task.TrackingTask
import org.ReDiego0.bastionCore.utils.ContractUtils
import org.bukkit.plugin.RegisteredServiceProvider
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
    lateinit var worldGuardManager: WorldGuardManager

    var economy: Economy? = null

    var citadelWorldName: String = "Bastion"

    override fun onEnable() {
        instance = this
        saveDefaultConfig()
        ContractUtils.reloadMissions()
        citadelWorldName = config.getString("citadel_world", "Bastion")!!

        worldGuardManager = WorldGuardManager()
        playerDataManager = PlayerDataManager(this)
        cooldownManager = CooldownManager()
        combatManager = CombatManager(this)
        vaultManager = VaultManager(this)
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
        server.pluginManager.registerEvents(BoardListener(this), this)
        server.pluginManager.registerEvents(GameListener(this), this)
        server.pluginManager.registerEvents(InstanceProtectionListener(this), this)

        getCommand("bastiondebug")?.setExecutor(org.ReDiego0.bastionCore.command.DebugCommand())
        getCommand("baul")?.setExecutor(org.ReDiego0.bastionCore.command.VaultCommand(this))
        getCommand("mision")?.setExecutor(org.ReDiego0.bastionCore.command.MissionCommand(this))
        getCommand("tablon")?.setExecutor(org.ReDiego0.bastionCore.command.BoardCommand(this))

        StaminaTask(this).runTaskTimer(this, 20L, 5L)
        TrackingTask(this).runTaskTimer(this, 20L, 2L)

        logger.info("§a[BastionCore] Sistemas de soporte vital activos. Ciudadela: $citadelWorldName")

        if (!setupEconomy()) {
            logger.severe("¡Desactivado! No se encontró la dependencia Vault o un plugin de economía (Essentials).")
            server.pluginManager.disablePlugin(this)
            return
        }
        if (server.pluginManager.getPlugin("PlaceholderAPI") != null) {
            org.ReDiego0.bastionCore.hooks.BastionExpansion(this).register()
            logger.info("[BastionCore] PlaceholderAPI detectado y conectado.")
        }
    }

    override fun onDisable() {
        instanceManager.cleanupAll()
        logger.info("§c[BastionCore] Cerrando conexión con el servidor central...")
    }

    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return false
        }
        val rsp: RegisteredServiceProvider<Economy>? = server.servicesManager.getRegistration(Economy::class.java)
        if (rsp == null) {
            return false
        }
        economy = rsp.provider
        return economy != null
    }
}