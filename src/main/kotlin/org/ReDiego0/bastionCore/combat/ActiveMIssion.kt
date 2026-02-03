package org.ReDiego0.bastionCore.combat

import java.util.*

data class ActiveMission(
    val worldName: String,
    val leaderId: UUID,

    val type: MissionType,
    val targetId: String,
    val requiredAmount: Int,
    var currentProgress: Int = 0,

    val rewardGold: Double,
    val threatLevel: Int,

    val allowedBlocks: Set<String> = emptySet()
)