package org.ReDiego0.bastionCore.task

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.listener.StaminaListener
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.scheduler.BukkitRunnable

class StaminaTask(private val plugin: BastionCore) : BukkitRunnable() {

    private var globalTicks = 0L

    private val taskPeriod = 5L

    override fun run() {
        globalTicks += taskPeriod

        val sprintInterval = plugin.config.getLong("stamina.sprint_drain_interval", 20L)
        val flyInterval = plugin.config.getLong("stamina.fly_drain_interval", 40L)
        val regenInterval = plugin.config.getLong("stamina.regen_interval", 20L)
        val regenDelay = plugin.config.getLong("stamina.regen_delay_ms", 2000L)

        val shouldDrainSprint = (globalTicks % sprintInterval) < taskPeriod
        val shouldDrainFly = (globalTicks % flyInterval) < taskPeriod
        val shouldRegen = (globalTicks % regenInterval) < taskPeriod

        val ultGainPerSecond = plugin.config.getDouble("combat.passive_ultimate_gain", 0.5)
        val ultGainThisTick = ultGainPerSecond / (20.0 / taskPeriod)


        for (player in Bukkit.getOnlinePlayers()) {
            if (player.gameMode == GameMode.CREATIVE || player.gameMode == GameMode.SPECTATOR) continue

            val data = plugin.playerDataManager.getData(player.uniqueId) ?: continue
            val currentWorld = player.world.name
            val isAtCitadel = currentWorld == plugin.citadelWorldName
            var isConsuming = false

            if (isAtCitadel) {
                if (player.isFlying) {
                    val minToFly = plugin.config.getInt("stamina.min_food_to_fly", 4)

                    if (player.foodLevel > 0) {
                        if (shouldDrainFly) {
                            StaminaListener.changeStamina(player, -1)
                        }
                        isConsuming = true
                    } else {
                        player.isFlying = false
                        player.allowFlight = false
                        player.sendMessage("§c[!] Energía agotada. Aterrizando...")
                    }

                }
                else if (!player.allowFlight && player.foodLevel > 4) {
                    player.allowFlight = true
                }

            }
            else {
                if (player.isSprinting) {
                    val minToSprint = plugin.config.getInt("stamina.min_food_to_sprint", 6)

                    if (player.foodLevel > 0) {
                        if (shouldDrainSprint) {
                            StaminaListener.changeStamina(player, -1)
                        }
                        isConsuming = true
                    }

                    if (player.foodLevel <= 1) {
                        player.isSprinting = false
                    }
                }

                if (data.ultimateCharge < 100.0) {
                    data.addCharge(player, ultGainThisTick)
                }
            }

            if (isConsuming) {
                data.lastStaminaUsage = System.currentTimeMillis()
            } else {
                val timeSinceLastAction = System.currentTimeMillis() - data.lastStaminaUsage

                if (timeSinceLastAction > regenDelay && player.foodLevel < 20) {
                    if (shouldRegen) {
                        val regenAmount = if (isAtCitadel) 1 else 1
                        StaminaListener.changeStamina(player, regenAmount)
                    }
                }
            }
        }
    }
}