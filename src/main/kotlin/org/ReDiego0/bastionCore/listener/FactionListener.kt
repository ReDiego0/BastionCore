package org.ReDiego0.bastionCore.listener

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.data.Faction
import org.ReDiego0.bastionCore.inventory.FactionHolder
import org.ReDiego0.bastionCore.inventory.FactionMenuType
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class FactionListener(private val plugin: BastionCore) : Listener {

    private val gui = org.ReDiego0.bastionCore.inventory.FactionGUI(plugin)

    @EventHandler
    fun onGuiClick(e: InventoryClickEvent) {
        if (e.inventory.holder !is FactionHolder) return
        e.isCancelled = true

        val player = e.whoClicked as? Player ?: return
        val holder = e.inventory.holder as FactionHolder
        val slot = e.slot

        val data = plugin.playerDataManager.getData(player.uniqueId) ?: return

        when (holder.type) {

            FactionMenuType.SELECTION -> {
                when (slot) {
                    11 -> {
                        if (data.faction != Faction.NONE) {
                            player.sendMessage("§cYa perteneces a una facción. Debes desertar primero.")
                            player.closeInventory()
                            return
                        }

                        data.joinFaction(player, holder.faction)
                        player.closeInventory()
                        player.world.strikeLightningEffect(player.location)
                    }
                    15 -> player.closeInventory()
                }
            }

            FactionMenuType.DASHBOARD -> {
                when (slot) {
                    20 -> {
                        player.closeInventory()
                        plugin.getCommand("tablon")?.execute(player, "tablon", emptyArray())
                    }
                    24 -> {
                        gui.openShop(player, 1)
                    }
                    49 -> {
                        handleLeaveFaction(player)
                    }
                }
            }

            FactionMenuType.SHOP -> {
                val currentLvl = holder.shopLevel

                when (slot) {
                    45 -> gui.openShop(player, currentLvl - 1)
                    53 -> gui.openShop(player, currentLvl + 1)
                    49 -> gui.openFactionHub(player)
                }
            }

            else -> {}
        }
    }

    private fun handleLeaveFaction(player: Player) {
        val data = plugin.playerDataManager.getData(player.uniqueId) ?: return
        val cooldownTime = 7 * 24 * 60 * 60 * 1000L
        val timeSinceJoin = System.currentTimeMillis() - data.lastFactionJoin

        if (timeSinceJoin < cooldownTime) {
            player.sendMessage("§cDebes esperar 7 días antes de desertar de tu facción.")
            return
        }

        player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
        player.sendMessage("§cHas abandonado ${data.faction.displayName}.")
        player.sendMessage("§7Todo tu progreso y rango ha sido eliminado.")

        data.faction = Faction.NONE
        data.factionLevel = 1
        data.factionXp = 0
        data.lastFactionJoin = 0

        player.closeInventory()
    }
}