package org.ReDiego0.bastionCore.hooks

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.utils.RankUtils
import org.bukkit.entity.Player

class BastionExpansion(private val plugin: BastionCore) : PlaceholderExpansion() {

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

        val data = plugin.playerDataManager.getData(player.uniqueId)

        if (params.equals("hunter_rank", ignoreCase = true)) {
            return data?.hunterRank?.toString() ?: "1"
        }

        if (params.equals("hunter_xp", ignoreCase = true)) {
            return data?.guildPoints?.toString() ?: "0"
        }

        if (params.equals("xp_needed", ignoreCase = true)) {
            val rank = data?.hunterRank ?: 1
            return RankUtils.getRequiredXp(rank).toString()
        }
        return null
    }
}