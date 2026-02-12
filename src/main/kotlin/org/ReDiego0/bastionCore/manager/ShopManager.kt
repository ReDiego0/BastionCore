package org.ReDiego0.bastionCore.manager

import dev.lone.itemsadder.api.CustomStack
import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.data.Faction
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.io.File

class ShopManager(private val plugin: BastionCore) {

    private val shopCache = HashMap<String, List<ShopItem>>()

    fun loadShops() {
        val file = File(plugin.dataFolder, "shops.yml")
        if (!file.exists()) plugin.saveResource("shops.yml", false)
        val config = YamlConfiguration.loadConfiguration(file)

        shopCache.clear()

        for (factionKey in config.getKeys(false)) {
            val section = config.getConfigurationSection(factionKey) ?: continue

            for (levelKey in section.getKeys(false)) {
                val level = levelKey.toIntOrNull() ?: continue
                val itemsList = ArrayList<ShopItem>()

                val levelSection = section.getConfigurationSection(levelKey) ?: continue
                for (itemId in levelSection.getKeys(false)) {
                    val path = "$factionKey.$levelKey.$itemId"

                    val itemStr = config.getString("$path.item")
                    val slot = config.getInt("$path.slot")
                    val price = config.getDouble("$path.price")

                    var stack: ItemStack? = null
                    if (itemStr != null) {
                        if (itemStr.contains(":")) {
                            stack = CustomStack.getInstance(itemStr)?.itemStack
                        } else {
                            stack = ItemStack(Material.getMaterial(itemStr) ?: Material.STONE)
                        }
                    }

                    if (stack != null) {
                        val meta = stack.itemMeta
                        val lore = meta.lore ?: ArrayList()
                        lore.add("")
                        lore.add("§7Precio: §6$price Oro")
                        lore.add("§e[Clic para comprar]")
                        meta.lore = lore

                        meta.persistentDataContainer.set(org.bukkit.NamespacedKey(plugin, "shop_price"), PersistentDataType.DOUBLE, price)
                        stack.itemMeta = meta

                        itemsList.add(ShopItem(slot, stack, price))
                    }
                }
                shopCache["$factionKey:$level"] = itemsList
            }
        }
        plugin.logger.info("Tiendas cargadas: ${shopCache.size} niveles configurados.")
    }

    fun getItemsFor(faction: Faction, level: Int): List<ShopItem> {
        return shopCache["${faction.name}:$level"] ?: emptyList()
    }

    data class ShopItem(val slot: Int, val item: ItemStack, val price: Double)
}