package org.ReDiego0.bastionCore.data

import java.util.*

data class Party(
    val id: UUID = UUID.randomUUID(),
    var leaderId: UUID,
    val members: MutableSet<UUID> = HashSet(),
    val maxSize: Int = 4
) {
    init {
        members.add(leaderId)
    }

    fun isLeader(uuid: UUID): Boolean = leaderId == uuid
    fun isFull(): Boolean = members.size >= maxSize

    fun getAllMembers(): Set<UUID> = members
}