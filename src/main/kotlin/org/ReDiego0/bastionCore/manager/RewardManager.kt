package org.ReDiego0.bastionCore.manager

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.combat.ActiveMission
import org.ReDiego0.bastionCore.data.Faction
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object RewardManager {

    fun grantReward(player: Player, gold: Double, items: List<ItemStack>, commands: List<String>, xp: Int = 0) {

        if (gold > 0) {
            val eco = BastionCore.instance.economy
            if (eco != null) {
                eco.depositPlayer(player, gold)
                player.sendMessage("§6+ $gold Oro")
            } else {
                BastionCore.instance.logger.warning("Error: No se pudo entregar oro a ${player.name} (Fallo en Economy Hook)")
            }
        }

        if (xp > 0) {
            val data = BastionCore.instance.playerDataManager.getData(player.uniqueId)
            data?.addGuildPoints(xp)
            player.sendMessage("§b+ $xp Puntos de Gremio")
        }

        for (item in items) {
            if (player.inventory.firstEmpty() != -1) {
                player.inventory.addItem(item)
            } else {
                player.world.dropItemNaturally(player.location, item)
                player.sendMessage("§cInventario lleno. Recompensa dejada en el suelo.")
            }
        }

        for (cmd in commands) {
            org.bukkit.Bukkit.dispatchCommand(
                org.bukkit.Bukkit.getConsoleSender(),
                cmd.replace("%player%", player.name)
            )
        }
    }

    fun handleMissionEnd(player: Player, mission: ActiveMission, isFaction: Boolean) {
        val data = BastionCore.instance.playerDataManager.getData(player.uniqueId) ?: return

        grantReward(player, mission.rewardGold, emptyList(), emptyList(), 100)

        if (isFaction && data.faction != Faction.NONE) {
            val factionXp = 250
            data.addFactionXp(factionXp)
            player.sendMessage("${data.faction.color}+ $factionXp Reputación con ${data.faction.displayName}")
        }
    }
}