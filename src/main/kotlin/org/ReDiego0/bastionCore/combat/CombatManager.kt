package org.ReDiego0.bastionCore.combat

import org.ReDiego0.bastionCore.BastionCore
import org.bukkit.Sound
import org.bukkit.entity.Player

class CombatManager(private val plugin: BastionCore) {

    fun handleRightClick(player: Player, weaponType: WeaponType) {
        // Aquí irá la lógica específica de cada arma (Switch)
        // Por ahora, feedback visual/sonoro genérico
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_IRON, 1f, 2f)
        player.sendMessage("§e[Combate] §fActivaste la Técnica Defensiva de: §6${weaponType.displayName}")
    }

    fun handleWeaponSkill(player: Player, weaponType: WeaponType) {
        // Consumir Vigor (Ejemplo)
        // if (player.foodLevel < 4) { "Sin vigor"; return }

        player.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.5f)
        player.sendMessage("§c[Combate] §f¡Habilidad de Arma (Q) de §6${weaponType.displayName}§f!")
    }

    fun handleClassUltimate(player: Player) {
        val data = plugin.playerDataManager.getData(player.uniqueId) ?: return
        val role = data.currentRole

        // Verificar si tiene carga (luego)
        // if (data.ultimateCharge < 100.0) return

        player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
        player.sendMessage("§b[CLASE] §f¡Activando Ultimate de §3${role.displayName}§f!")
        player.sendMessage("§7>> ${role.description}")
    }
}