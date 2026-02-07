package org.ReDiego0.bastionCore.combat

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.combat.weapons.*
import org.ReDiego0.bastionCore.manager.CooldownManager
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class CombatManager(private val plugin: BastionCore) {

    val greatswordHandler = GreatswordHandler(plugin)
    val spearHandler = SpearHandler(plugin)
    val hammerHandler = HammerHandler(plugin)
    val katanaHandler = KatanaHandler(plugin)
    val dualBladesHandler = DualBladesHandler(plugin)
    val bowHandler = BowHandler(plugin)

    private val blockingPlayers = ConcurrentHashMap<UUID, Long>()
    private val parryPlayers = ConcurrentHashMap<UUID, Long>()

    fun handleRightClick(player: Player, weaponType: WeaponType) {
        if (plugin.cooldownManager.checkAndNotify(player, CooldownManager.CooldownType.WEAPON_SECONDARY)) return

        when (weaponType) {
            WeaponType.GREATSWORD -> greatswordHandler.handleRightClick(player)
            WeaponType.SPEAR -> spearHandler.handleRightClick(player)
            WeaponType.HAMMER -> hammerHandler.handleRightClick(player)
            WeaponType.KATANA -> katanaHandler.handleRightClick(player)
            WeaponType.DUAL_BLADES -> dualBladesHandler.handleRightClick(player)
            WeaponType.BOW -> bowHandler.handleRightClick(player)
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
            WeaponType.GREATSWORD -> greatswordHandler.handlePrimary(player)
            WeaponType.SPEAR -> spearHandler.handlePrimary(player)
            WeaponType.HAMMER -> hammerHandler.handlePrimary(player)
            WeaponType.KATANA -> katanaHandler.handlePrimary(player)
            WeaponType.DUAL_BLADES -> dualBladesHandler.handlePrimary(player)
            WeaponType.BOW -> bowHandler.handlePrimary(player)
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
}