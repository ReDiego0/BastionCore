package org.ReDiego0.bastionCore.utils

object RankUtils {
    // Por ahora una formula sencilla ya que no sé sumar
    fun getRequiredXp(currentRank: Int): Int {
        return currentRank * 100
    }

    fun getProgress(currentXp: Int, currentRank: Int): Float {
        val req = getRequiredXp(currentRank).toFloat()
        return (currentXp / req).coerceIn(0f, 1f)
    }

    fun getFactionRequiredXp(level: Int): Int {
        return when (level) {
            1 -> 1500
            2 -> 3500
            3 -> 7500
            4 -> 15000
            else -> Int.MAX_VALUE
        }
    }

    fun getFactionProgress(currentXp: Int, level: Int): Float {
        if (level >= 5) return 1.0f
        val req = getFactionRequiredXp(level)
        return (currentXp.toFloat() / req.toFloat()).coerceIn(0.0f, 1.0f)
    }
}