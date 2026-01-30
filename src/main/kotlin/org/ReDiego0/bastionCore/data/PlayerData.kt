package org.ReDiego0.bastionCore.data

import org.ReDiego0.bastionCore.combat.Role
import java.util.UUID

data class PlayerData(
    val uuid: UUID,
    val name: String,
    var currentRole: Role = Role.RECLUTA,
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