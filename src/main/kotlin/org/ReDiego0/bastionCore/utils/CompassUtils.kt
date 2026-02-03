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
        val rotation = (player.location.yaw - 180) % 360
        val targetVector = target.toVector().subtract(player.location.toVector())
        val targetAngle = (Math.toDegrees(Math.atan2(targetVector.z, targetVector.x)) - 90) % 360
        var diff = (targetAngle - rotation)
        while (diff < -180) diff += 360
        while (diff > 180) diff -= 360

        val distance = player.location.distance(target).toInt()
        return when {
            diff in -10.0..10.0 -> "§8--§a[§c▼§a]§8-- §f${distance}m"
            diff in -45.0..-10.0 -> "§8---§e»§8-- §f${distance}m"
            diff in 10.0..45.0 -> "§f${distance}m §8--§e«§8---"
            else -> "§7Buscando señal..."
        }
    }

    private fun getAngle(v1: Vector, v2: Vector): Double {
        return Math.acos(v1.dot(v2) / (v1.length() * v2.length()))
    }
}