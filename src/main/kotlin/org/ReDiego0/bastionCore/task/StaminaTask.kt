package org.ReDiego0.bastionCore.task

import org.ReDiego0.bastionCore.BastionCore
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.scheduler.BukkitRunnable

class StaminaTask(private val plugin: BastionCore) : BukkitRunnable() {

    override fun run() {
        for (player in Bukkit.getOnlinePlayers()) {
            if (player.gameMode == GameMode.CREATIVE || player.gameMode == GameMode.SPECTATOR) continue

            val data = plugin.playerDataManager.getData(player.uniqueId) ?: continue
            val currentWorld = player.world.name
            val isAtCitadel = currentWorld == plugin.citadelWorldName
            var isConsuming = false

            if (isAtCitadel) {
                if (player.isFlying) {
                    if (player.foodLevel > 0) {
                        drainStamina(player, 1)
                        isConsuming = true
                    } else {
                        player.isFlying = false
                        player.allowFlight = false
                        player.sendMessage("§c[Aviso] §fPropulsores sin presión. Aterrizando...")
                    }

                }

                else if (!player.allowFlight && player.foodLevel > 4) {
                    player.allowFlight = true
                }

            } else {
                if (player.isSprinting) {
                    if (player.foodLevel > 0) {
                        drainStamina(player, 1)
                        isConsuming = true
                    }

                    if (player.foodLevel <= 1) {
                        player.isSprinting = false
                    }
                }
            }

            if (isConsuming) {
                data.lastStaminaUsage = System.currentTimeMillis()
            } else {
                val timeSinceLastAction = System.currentTimeMillis() - data.lastStaminaUsage
                if (timeSinceLastAction > 2000 && player.foodLevel < 20) {
                    // En Bastion: +1 (Lento, para no abusar del vuelo)
                    // En Caza: +2 (Rápido, para agarrarse a wates)
                    val regenAmount = if (isAtCitadel) 1 else 2
                    player.foodLevel = (player.foodLevel + regenAmount).coerceAtMost(20)
                }
            }
        }
    }

    private fun drainStamina(player: org.bukkit.entity.Player, amount: Int) {
        player.foodLevel = (player.foodLevel - amount).coerceAtLeast(0)
    }
}