package org.ReDiego0.bastionCore.manager

import org.ReDiego0.bastionCore.BastionCore
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.ThreadLocalRandom

class BoardCycleManager(private val plugin: BastionCore) {
    private var nextRotationTime: Long = 0
    private val currentStock = ArrayList<ItemStack>()

    private val rotationMinutes: Int
        get() = plugin.config.getInt("settings.board_rotation_minutes", 60)

    init {
        refreshStock()
        startTimer()
    }

    private fun startTimer() {
        object : BukkitRunnable() {
            override fun run() {
                if (System.currentTimeMillis() >= nextRotationTime) {
                    refreshStock()
                }
            }
        }.runTaskTimer(plugin, 20L, 1200L)
    }

    fun refreshStock() {
        nextRotationTime = System.currentTimeMillis() + (rotationMinutes * 60 * 1000)

        currentStock.clear()

        val generator = MissionGenerator(plugin)
        val templates = plugin.config.getConfigurationSection("templates")?.getKeys(false) ?: return

        for (i in 0 until 9) {
            val randomTemplate = templates.elementAt(ThreadLocalRandom.current().nextInt(templates.size))
            val item = generator.generateMission(randomTemplate)
            if (item != null) {
                currentStock.add(item)
            }
        }

        plugin.logger.info("§a[BastionBoard] ¡Nuevos contratos disponibles! Próxima rotación en ${rotationMinutes}m.")
    }

    fun takeMission(originalItem: ItemStack): ItemStack {
        val copy = originalItem.clone()
        return org.ReDiego0.bastionCore.utils.ContractUtils.stampExpiration(copy)
    }

    fun getAvailableMissions(): List<ItemStack> {
        return currentStock
    }

    fun getTimeRemainingSeconds(): Long {
        val diff = nextRotationTime - System.currentTimeMillis()
        return if (diff < 0) 0 else diff / 1000
    }
}