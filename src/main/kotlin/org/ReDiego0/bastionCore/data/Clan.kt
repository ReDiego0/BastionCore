package org.ReDiego0.bastionCore.data

import org.bukkit.Location
import java.util.*
import java.util.concurrent.ConcurrentHashMap

data class Clan(
    val id: String,
    var displayName: String,
    var leaderUuid: UUID,
    var homeLocation: Location?,
    val createdDate: Long = System.currentTimeMillis()
) {
    val members = ConcurrentHashMap<UUID, Int>()
    val rankNames = ConcurrentHashMap<Int, String>()
    var balance: Double = 0.0
    init {
        for (rank in ClanRank.entries) {
            rankNames[rank.level] = rank.defaultName
        }
        members[leaderUuid] = ClanRank.TAISHO.level
    }

    fun getRankDisplayName(level: Int): String {
        return rankNames[level] ?: ClanRank.fromLevel(level).defaultName
    }

    fun addMember(uuid: UUID, rank: ClanRank = ClanRank.ASHIGARU) {
        members[uuid] = rank.level
    }

    fun removeMember(uuid: UUID) {
        members.remove(uuid)
    }

    fun isMember(uuid: UUID): Boolean = members.containsKey(uuid)

    fun getMemberRank(uuid: UUID): ClanRank {
        val level = members[uuid] ?: return ClanRank.ASHIGARU
        return ClanRank.fromLevel(level)
    }

    fun setMemberRank(uuid: UUID, rank: ClanRank) {
        if (members.containsKey(uuid)) {
            members[uuid] = rank.level
        }
    }
}