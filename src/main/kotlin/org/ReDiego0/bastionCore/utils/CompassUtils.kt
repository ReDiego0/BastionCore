package org.ReDiego0.bastionCore.utils

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector

object CompassUtils {

    private const val SYMBOL_COMPASS = "|"
    private const val SYMBOL_TARGET = "§c▼"

    fun getCompassBar(player: Player, target: Location): String {
        val playerDir = player.location.direction
        val targetDir = target.toVector().subtract(player.location.toVector()).normalize()

        val angle = getAngle(playerDir, targetDir)
        val distance = player.location.distance(target).toInt()

        return if (angle < 0.5) {
            "§a$distance m §f[ $SYMBOL_TARGET §f]"
        } else {
            val cross = playerDir.crossProduct(targetDir).y
            if (cross > 0) "§e◄ $distance m" else "§e$distance m ►"
        }
    }

    fun getSkyrimCompass(player: Player, target: Location): String {
        val playerYaw = normalizeYaw(player.location.yaw)
        val dirToTarget = target.toVector().subtract(player.location.toVector()).normalize()
        val lookLoc = player.location.clone()
        lookLoc.direction = dirToTarget
        val targetYaw = normalizeYaw(lookLoc.yaw)

        var diff = targetYaw - playerYaw
        if (diff < -180) diff += 360
        if (diff > 180) diff -= 360

        val distance = player.location.distance(target).toInt()
        return when {
            diff in -15.0..15.0 -> "§8--§a[ §c▼ §a]§8-- §f${distance}m"
            diff in -45.0..-15.0 -> "§8---§e«§8--- §f${distance}m"
            diff in 15.0..45.0 -> "§8---§e»§8--- §f${distance}m"
            diff in -135.0..-45.0 -> "§e«« §7Izquierda §f${distance}m"
            diff in 45.0..135.0 -> "§f${distance}m §7Derecha §e»»"
            else -> "§c⬇ §7Detrás §c⬇ §f${distance}m"
        }
    }

    private fun normalizeYaw(yaw: Float): Float {
        var newYaw = yaw % 360
        if (newYaw < 0) newYaw += 360
        return newYaw
    }

    private fun getAngle(v1: Vector, v2: Vector): Double {
        return Math.acos(v1.dot(v2) / (v1.length() * v2.length()))
    }
}