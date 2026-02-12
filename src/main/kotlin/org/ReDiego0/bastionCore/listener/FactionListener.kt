package org.ReDiego0.bastionCore.listener

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.data.Faction
import org.ReDiego0.bastionCore.inventory.FactionHolder
import org.ReDiego0.bastionCore.inventory.FactionMenuType
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.ItemStack

class FactionListener(private val plugin: BastionCore) : Listener {

    private val gui = org.ReDiego0.bastionCore.inventory.FactionGUI(plugin)

    @EventHandler
    fun onGuiDrag(e: InventoryDragEvent) {
        if (e.inventory.holder is FactionHolder) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onGuiClick(e: InventoryClickEvent) {
        if (e.inventory.holder !is FactionHolder) return
        e.isCancelled = true
        if (e.clickedInventory == null) return

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
                    else -> {
                        val itemClicked = e.currentItem
                        if (itemClicked != null && itemClicked.type != Material.AIR) {
                            val meta = itemClicked.itemMeta
                            val key = org.bukkit.NamespacedKey(plugin, "shop_price")

                            if (meta.persistentDataContainer.has(key, org.bukkit.persistence.PersistentDataType.DOUBLE)) {
                                val price = meta.persistentDataContainer.get(key, org.bukkit.persistence.PersistentDataType.DOUBLE) ?: 0.0
                                handlePurchase(player, itemClicked, price)
                            }
                        }
                    }
                }
            }

            else -> {}
        }
    }

    private fun handlePurchase(player: Player, item: ItemStack, price: Double) {
        val economy = plugin.economy ?: return

        if (!economy.has(player, price)) {
            player.sendMessage("§cNo tienes suficiente oro. Requiere: $price")
            player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
            return
        }

        if (player.inventory.firstEmpty() == -1) {
            player.sendMessage("§cTu inventario está lleno.")
            return
        }

        economy.withdrawPlayer(player, price)

        val toGive = item.clone()
        val meta = toGive.itemMeta
        val lore = meta.lore ?: ArrayList()
        if (lore.size >= 2) {
            lore.removeAt(lore.size - 1)
            lore.removeAt(lore.size - 1)
        }
        meta.lore = lore
        meta.persistentDataContainer.remove(org.bukkit.NamespacedKey(plugin, "shop_price"))
        toGive.itemMeta = meta

        player.inventory.addItem(toGive)

        player.sendMessage("§a¡Compra realizada! - $price Oro")
        player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f)
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