package org.ReDiego0.bastionCore.combat

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.manager.CooldownManager
import org.bukkit.Sound
import org.bukkit.entity.Player

class CombatManager(private val plugin: BastionCore) {
    private val cooldowns = plugin.cooldownManager

    fun handleRightClick(player: Player, weaponType: WeaponType) {
        if (cooldowns.checkAndNotify(player, CooldownManager.CooldownType.WEAPON_SECONDARY)) return
        when (weaponType) {
            WeaponType.KATANA -> {
                player.sendMessage("§e[Katana] §fPostura de Desvío iniciada.")
                player.playSound(player.location, Sound.BLOCK_ANVIL_LAND, 1f, 2f)

                cooldowns.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY, 5.0)
            }
            WeaponType.GREATSWORD -> {
                player.sendMessage("§6[Gran Espada] §fBloqueo pesado activo.")
                player.playSound(player.location, Sound.ITEM_SHIELD_BLOCK, 1f, 0.5f)

                cooldowns.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_SECONDARY, 2.0)
            }
            else -> {
                player.sendMessage("§7Esta arma no tiene técnica defensiva aún.")
            }
        }
    }

    fun handleWeaponSkill(player: Player, weaponType: WeaponType) {
        if (cooldowns.checkAndNotify(player, CooldownManager.CooldownType.WEAPON_PRIMARY)) return

        if (player.foodLevel < 4) {
            player.sendMessage("§c¡Demasiado exhausto para usar habilidades!")
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f)
            return
        }

        when (weaponType) {
            WeaponType.KATANA -> {
                player.sendMessage("§e[Katana] §f¡Corte Relámpago!")
                player.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1.5f)

                org.ReDiego0.bastionCore.listener.StaminaListener.changeStamina(player, -4)
                cooldowns.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 8.0)
            }
            WeaponType.GREATSWORD -> {
                player.sendMessage("§6[Gran Espada] §f¡Carga Sísmica!")
                player.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.5f)

                org.ReDiego0.bastionCore.listener.StaminaListener.changeStamina(player, -6)
                cooldowns.setCooldown(player.uniqueId, CooldownManager.CooldownType.WEAPON_PRIMARY, 12.0)
            }
            else -> {
                player.sendMessage("§7Habilidad no implementada.")
            }
        }
    }

    fun handleClassUltimate(player: Player) {
        if (cooldowns.checkAndNotify(player, CooldownManager.CooldownType.CLASS_ULTIMATE)) return
        val data = plugin.playerDataManager.getData(player.uniqueId) ?: return

        if (data.ultimateCharge < 100.0) {
            player.sendMessage("§cLa Ultimate no está lista (${data.ultimateCharge.toInt()}%)")
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f)
            return
        }

        val role = data.currentRole
        player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
        player.sendMessage("§b[CLASE] §f¡Habilidad Definitiva de ${role.displayName}!")

        // Aquí irían los efectos reales (Curar, Daño masivo, Inmortalidad...)

        data.resetCharge(player)

        cooldowns.setCooldown(player.uniqueId, CooldownManager.CooldownType.CLASS_ULTIMATE, 5.0)
    }
}