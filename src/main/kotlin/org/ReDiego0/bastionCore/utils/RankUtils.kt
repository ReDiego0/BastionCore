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
}