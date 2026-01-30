package org.ReDiego0.bastionCore.data

import java.util.UUID

data class PlayerData(
    val uuid: UUID,
    val name: String,
    var roleId: String = "recluta",
    var ultimateCharge: Double = 0.0,
    var lastStaminaUsage: Long = System.currentTimeMillis()
) {

    fun addCharge(amount: Double) {
        ultimateCharge = (ultimateCharge + amount).coerceIn(0.0, 100.0)
    }

    fun resetCharge() {
        ultimateCharge = 0.0
    }
}