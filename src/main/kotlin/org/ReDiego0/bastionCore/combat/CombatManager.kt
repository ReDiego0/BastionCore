package org.ReDiego0.bastionCore.combat

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.combat.weapons.*
import org.ReDiego0.bastionCore.manager.CooldownManager
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class CombatManager(private val plugin: BastionCore) {

    val katanaHandler = KatanaHandler(plugin)
    val nodachiHandler = NodachiHandler(plugin)
    val yumiHandler = YumiHandler(plugin)
    val naginataHandler = NaginataHandler(plugin)
    val tekkoHandler = TekkoHandler(plugin)
    val kamaHandler = KamaHandler(plugin)

    private val blockingPlayers = ConcurrentHashMap<UUID, Long>()
    private val parryPlayers = ConcurrentHashMap<UUID, Long>()

    fun handleRightClick(player: Player, weaponType: WeaponType) {
        if (plugin.cooldownManager.checkAndNotify(player, CooldownManager.CooldownType.WEAPON_SECONDARY)) return

        when (weaponType) {
            WeaponType.KATANA -> katanaHandler.handleRightClick(player)
            WeaponType.NODACHI -> nodachiHandler.handleRightClick(player)
            WeaponType.YUMI -> yumiHandler.handleRightClick(player)
            WeaponType.NAGINATA -> naginataHandler.handleRightClick(player)
            WeaponType.TEKKO -> tekkoHandler.handleRightClick(player)
            WeaponType.KAMA -> kamaHandler.handleRightClick(player)
            else -> {}
        }
    }

    fun handleWeaponPrimary(player: Player, weaponType: WeaponType) {
        if (plugin.cooldownManager.checkAndNotify(player, CooldownManager.CooldownType.WEAPON_PRIMARY)) return

        if (player.foodLevel < 2) {
            player.sendMessage("§c¡Estás exhausto!")
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f)
            return
        }

        when (weaponType) {
            WeaponType.KATANA -> katanaHandler.handlePrimary(player)
            WeaponType.NODACHI -> nodachiHandler.handlePrimary(player)
            WeaponType.YUMI -> yumiHandler.handlePrimary(player)
            WeaponType.NAGINATA -> naginataHandler.handlePrimary(player)
            WeaponType.TEKKO -> tekkoHandler.handlePrimary(player)
            WeaponType.KAMA -> kamaHandler.handlePrimary(player)
            else -> {}
        }
    }

    fun handleClassUltimate(player: Player) {
        if (plugin.cooldownManager.checkAndNotify(player, CooldownManager.CooldownType.CLASS_ULTIMATE)) return

        val data = plugin.playerDataManager.getData(player.uniqueId) ?: return

        if (data.ultimateCharge < 100.0) {
            player.sendMessage("§cHabilidad de Clase no lista (${data.ultimateCharge.toInt()}%)")
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f)
            return
        }

        data.ultimateCharge = 0.0

        player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
        player.sendMessage("§b[CLASE] §f¡Habilidad Definitiva Activada!")

        plugin.roleManager.activateUltimate(player, data.currentRole)
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.CLASS_ULTIMATE, 30.0)
    }

    fun setBlocking(uuid: UUID, ticks: Long) {
        blockingPlayers[uuid] = System.currentTimeMillis() + (ticks * 50)
    }
    fun isBlocking(uuid: UUID): Boolean {
        val time = blockingPlayers[uuid] ?: return false
        if (System.currentTimeMillis() > time) {
            blockingPlayers.remove(uuid)
            return false
        }
        return true
    }
    fun removeBlocking(uuid: UUID) { blockingPlayers.remove(uuid) }

    fun setParry(uuid: UUID, ticks: Long) {
        parryPlayers[uuid] = System.currentTimeMillis() + (ticks * 50)
    }
    fun isParrying(uuid: UUID): Boolean {
        val time = parryPlayers[uuid] ?: return false
        if (System.currentTimeMillis() > time) {
            parryPlayers.remove(uuid)
            return false
        }
        return true
    }
    fun removeParry(uuid: UUID) { parryPlayers.remove(uuid) }

    fun handleDirectionalDash(player: Player) {
        val playerId = player.uniqueId

        if (plugin.cooldownManager.isOnCooldown(playerId, CooldownManager.CooldownType.DASH)) {
            return
        }

        val velocity = player.velocity
        val horizontalVelocity = velocity.clone().setY(0)

        if (horizontalVelocity.lengthSquared() < 0.01) {
            val backDir = player.location.direction.clone().setY(0).normalize().multiply(-1)
            performDash(player, backDir, "§7Evasión Atrás", 1.0)
            return
        }

        val targetDir = horizontalVelocity.normalize()
        val playerDir = player.location.direction.clone().setY(0).normalize()
        val dot = targetDir.dot(playerDir)

        var dashType = "Dash"
        var power = 1.5

        if (dot > 0.5) {
            dashType = "§bDash Frontal"
            power = 1.8
        } else if (dot < -0.5) {
            dashType = "§7Retirada"
            power = 1.4
        } else {
            val crossY = (playerDir.x * targetDir.z) - (playerDir.z * targetDir.x)
            if (crossY > 0) {
                dashType = "§eDash Derecha"
            } else {
                dashType = "§eDash Izquierda"
            }
            power = 1.6
        }

        performDash(player, targetDir, dashType, power)
    }

    private fun performDash(player: Player, direction: Vector, name: String, power: Double) {
        plugin.cooldownManager.setCooldown(player.uniqueId, CooldownManager.CooldownType.DASH, 2.0)
        player.velocity = direction.multiply(power).setY(0.4)
        player.noDamageTicks = 20
        player.world.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 2f)
        player.world.spawnParticle(org.bukkit.Particle.CLOUD, player.location, 5, 0.2, 0.1, 0.2, 0.05)
        player.sendActionBar(name)
    }
}