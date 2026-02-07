package org.ReDiego0.bastionCore.data

import org.ReDiego0.bastionCore.combat.Role
import org.ReDiego0.bastionCore.utils.RankUtils
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*

data class PlayerData(val uuid: UUID, val name: String) {
    var currentRole: Role = Role.RECLUTA
    var ultimateCharge: Double = 0.0
    var hunterRank: Int = 1
    var guildPoints: Int = 0
    var reputationProgress: Float = 0.0f
    var lastStaminaUsage: Long = System.currentTimeMillis()


    fun addCharge(player: Player, amount: Double): Boolean {
        val wasFull = ultimateCharge >= 100.0
        ultimateCharge = (ultimateCharge + amount).coerceIn(0.0, 100.0)

        val citadelName = org.ReDiego0.bastionCore.BastionCore.instance.citadelWorldName

        if (player.world.name != citadelName) {
            player.level = ultimateCharge.toInt()
            player.exp = (ultimateCharge / 100.0).toFloat()
        }

        val isFull = ultimateCharge >= 100.0
        if (!wasFull && isFull) {
            if (player.world.name != citadelName) {
                player.playSound(player.location, Sound.BLOCK_BEACON_ACTIVATE, 1f, 2f)
                player.sendMessage("§b⚡ ¡ULTIMATE LISTA! ⚡")
            }
            return true
        }
        return false
    }

    fun resetCharge(player: Player) {
        ultimateCharge = 0.0
        player.level = 0
        player.exp = 0f
    }

    fun addGuildPoints(amount: Int) {
        guildPoints += amount
        checkLevelUp()
    }

    private fun checkLevelUp() {
        val player = Bukkit.getPlayer(uuid) ?: return
        var leveledUp = false

        while (guildPoints >= RankUtils.getRequiredXp(hunterRank)) {
            guildPoints -= RankUtils.getRequiredXp(hunterRank)
            hunterRank++
            leveledUp = true
        }

        if (leveledUp) {
            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
            player.sendTitle("§b¡RANGO SUBIDO!", "§7Ahora eres Rango §e$hunterRank", 10, 70, 20)
            player.sendMessage("§a[Gremio] §f¡Felicidades! Has alcanzado el Rango de Cazador $hunterRank.")
            player.sendMessage("§7Nuevos contratos de mayor amenaza disponibles.")
        }
    }

}