package org.ReDiego0.bastionCore.manager

import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class CooldownManager {

    enum class CooldownType {
        WEAPON_PRIMARY,   // Tecla Q
        WEAPON_SECONDARY, // Click Derecho
        CLASS_ULTIMATE,   // Tecla F
        DASH              // Shift + Movimiento (NUEVO)
    }

    private val cooldowns = ConcurrentHashMap<UUID, MutableMap<CooldownType, Long>>()

    fun getRemainingSeconds(uuid: UUID, type: CooldownType): Double {
        val playerCooldowns = cooldowns[uuid] ?: return 0.0
        val expiryTime = playerCooldowns[type] ?: return 0.0

        val timeLeft = expiryTime - System.currentTimeMillis()
        if (timeLeft <= 0) {
            playerCooldowns.remove(type)
            return 0.0
        }
        return timeLeft / 1000.0
    }

    fun setCooldown(uuid: UUID, type: CooldownType, seconds: Double) {
        val expiryTime = System.currentTimeMillis() + (seconds * 1000).toLong()
        cooldowns.computeIfAbsent(uuid) { ConcurrentHashMap() }[type] = expiryTime
    }

    fun isOnCooldown(uuid: UUID, type: CooldownType): Boolean {
        return getRemainingSeconds(uuid, type) > 0.0
    }

    fun checkAndNotify(player: Player, type: CooldownType): Boolean {
        val remaining = getRemainingSeconds(player.uniqueId, type)
        if (remaining > 0) {
            val formatted = String.format("%.1f", remaining)
            player.sendActionBar(net.kyori.adventure.text.Component.text("§cHabilidad en espera: $formatted s"))
            return true
        }
        return false
    }
}