package org.ReDiego0.bastionCore.hooks

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.utils.RankUtils
import org.bukkit.entity.Player
import java.text.DecimalFormat

class BastionExpansion(private val plugin: BastionCore) : PlaceholderExpansion() {

    private val decimalFormat = DecimalFormat("#.##")

    override fun getIdentifier(): String = "bastion"
    override fun getAuthor(): String = "ReDiego0"
    override fun getVersion(): String = plugin.description.version

    override fun canRegister(): Boolean = true
    override fun persist(): Boolean = true

    override fun onPlaceholderRequest(player: Player?, params: String): String? {

        if (params.equals("board_timer", ignoreCase = true)) {
            val secondsLeft = plugin.boardCycleManager.getTimeRemainingSeconds()
            val minutes = secondsLeft / 60
            val seconds = secondsLeft % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

        if (player == null) return ""

        val data = plugin.playerDataManager.getData(player.uniqueId) ?: return "Loading..."

        if (params.equals("hunter_rank", ignoreCase = true)) {
            return data.hunterRank.toString()
        }

        if (params.equals("hunter_xp", ignoreCase = true)) {
            return data.guildPoints.toString()
        }

        if (params.equals("xp_needed", ignoreCase = true)) {
            val rank = data.hunterRank
            return RankUtils.getRequiredXp(rank).toString()
        }

        if (params.equals("hunter_xp_progress", ignoreCase = true)) {
            return data.reputationProgress.toString()
        }

        if (params.equals("hunter_xp_percent", ignoreCase = true)) {
            val percent = (data.reputationProgress * 100).toInt()
            return "$percent%"
        }

        if (params.equals("faction_level", ignoreCase = true)) {
            return data.factionLevel.toString()
        }

        if (params.equals("faction_xp", ignoreCase = true)) {
            return decimalFormat.format(data.factionXp)
        }

        if (params.equals("faction_xp_percent", ignoreCase = true)) {
            val percent = (data.factionXp / 100.0).coerceIn(0.0, 1.0)
            return decimalFormat.format(percent * 100)
        }

        if (params.equals("faction_name", ignoreCase = true)) {
            return data.faction.displayName
        }

        if (params.equals("ultimate_charge", ignoreCase = true)) {
            return data.ultimateCharge.toInt().toString()
        }

        if (params.equals("ultimate_progress", ignoreCase = true)) {
            val progress = (data.ultimateCharge / 100.0).coerceIn(0.0, 1.0)
            return progress.toString()
        }

        if (params.equals("ultimate_formatted", ignoreCase = true)) {
            return "${data.ultimateCharge.toInt()}%"
        }

        if (params.equals("ultimate_ready", ignoreCase = true)) {
            return (data.ultimateCharge >= 100.0).toString()
        }

        if (params.equals("class_name", ignoreCase = true)) {
            return data.currentRole.name
        }

        val worldName = player.world.name
        val activeMission = plugin.gameManager.getMission(worldName)

        if (params.equals("mission_lives", ignoreCase = true)) {
            return activeMission?.currentLives?.toString() ?: "-"
        }

        if (params.equals("mission_lives_max", ignoreCase = true)) {
            return activeMission?.maxLives?.toString() ?: "-"
        }

        if (params.equals("mission_timer", ignoreCase = true)) {
            if (activeMission == null) return "--:--"
            val timeLeft = activeMission.timeLimitSeconds - activeMission.timeElapsed
            val minutes = timeLeft / 60
            val seconds = timeLeft % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

        if (params.equals("mission_lives_visual", ignoreCase = true)) {
            if (activeMission == null) return ""
            return generateLivesVisual(activeMission.currentLives, activeMission.maxLives)
        }

        return null
    }

    private fun generateLivesVisual(current: Int, max: Int): String {
        val activeSymbol = plugin.config.getString("gameplay.life_symbol_active", "&c❤")!!.replace("&", "§")
        val lostSymbol = plugin.config.getString("gameplay.life_symbol_lost", "&8☠")!!.replace("&", "§")
        val sb = StringBuilder()
        repeat(current) { sb.append(activeSymbol) }
        val lost = max - current
        if (lost > 0) {
            repeat(lost) { sb.append(lostSymbol) }
        }
        return sb.toString()
    }
}