package org.ReDiego0.bastionCore.combat

import java.util.UUID

data class ActiveMission(
    val worldName: String,
    val leaderId: UUID,
    val bossId: String,
    val rewardGold: Double,
    val threatLevel: Int,
    val startTime: Long = System.currentTimeMillis()
)