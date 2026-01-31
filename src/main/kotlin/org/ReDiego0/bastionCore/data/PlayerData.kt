package org.ReDiego0.bastionCore.data

import org.ReDiego0.bastionCore.combat.Role
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.UUID

data class PlayerData(
    val uuid: UUID,
    val name: String,
    var currentRole: Role = Role.RECLUTA,
    var ultimateCharge: Double = 0.0,
    var hunterRank: Int = 1,
    var reputationProgress: Float = 0.0f,
    var lastStaminaUsage: Long = System.currentTimeMillis()
) {

    fun addCharge(player: Player, amount: Double): Boolean {
        val wasFull = ultimateCharge >= 100.0
        ultimateCharge = (ultimateCharge + amount).coerceIn(0.0, 100.0)

        player.level = ultimateCharge.toInt()
        player.exp = (ultimateCharge / 100.0).toFloat()

        val isFull = ultimateCharge >= 100.0
        if (!wasFull && isFull) {
            player.playSound(player.location, Sound.BLOCK_BEACON_ACTIVATE, 1f, 2f)
            player.sendMessage("§b⚡ ¡ULTIMATE LISTA! (Presiona F) ⚡")
            return true
        }
        return false
    }

    fun resetCharge(player: Player) {
        ultimateCharge = 0.0
        player.level = 0
        player.exp = 0f
    }
}