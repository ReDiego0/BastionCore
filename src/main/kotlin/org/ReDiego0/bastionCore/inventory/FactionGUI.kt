package org.ReDiego0.bastionCore.inventory

import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.data.Faction
import org.ReDiego0.bastionCore.utils.RankUtils
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class FactionGUI(private val plugin: BastionCore) {

    fun openFactionHub(player: Player) {
        val data = plugin.playerDataManager.getData(player.uniqueId) ?: return
        if (data.faction == Faction.NONE) {
            player.sendMessage("§cError: No tienes facción para ver el Dashboard.")
            return
        }

        val holder = FactionHolder(FactionMenuType.DASHBOARD)
        val title = "Faction Dashboard" //":offset_-8::faction_${data.faction.displayName}:"
        val inv = Bukkit.createInventory(holder, 54, title)

        inv.setItem(20, createButton(
            Material.PAPER,
            "§e📜 Contratos de Facción",
            listOf("§7Realiza encargos para", "§7subir tu reputación.")
        ))

        inv.setItem(24, createButton(
            Material.GOLD_INGOT,
            "§6💰 Armería de Facción",
            listOf("§7Accede al equipo exclusivo.", "§7Nivel Actual: §e${data.factionLevel}")
        ))

        inv.setItem(49, createButton(
            Material.BARRIER,
            "§c⚠ Abandonar Facción",
            listOf("§7Perderás todo el progreso.", "§7Cooldown de 7 días.")
        ))

        val statsItem = createButton(data.faction.icon, "${data.faction.color}${data.faction.displayName}",
            listOf(
                "§fNivel: §e${data.factionLevel}/5",
                "§fXP: §b${data.factionXp} / ${RankUtils.getFactionRequiredXp(data.factionLevel)}"
            )
        )
        inv.setItem(4, statsItem)

        player.openInventory(inv)
    }

    fun openJoinMenu(player: Player, faction: Faction) {
        val holder = FactionHolder(FactionMenuType.SELECTION, faction = faction)
        val inv = Bukkit.createInventory(holder, 27, "§8Juramento: ${faction.displayName}")

        val infoItem = createButton(faction.icon, "${faction.color}Fidelidad a ${faction.displayName}",
            listOf(
                "§7${faction.description}",
                "",
                "§e⚠ ADVERTENCIA:",
                "§7- No podrás unirte a otra facción.",
                "§7- Abandonarla reseteará tu progreso.",
                "",
                "§6[Haz clic en la bandera para jurar]"
            )
        )
        inv.setItem(13, infoItem)
        inv.setItem(11, createButton(Material.LIME_CONCRETE, "§a¡ACEPTO EL PACTO!", listOf("§7Deseo servir a esta facción.")))
        inv.setItem(15, createButton(Material.BARRIER, "§cLo pensaré mejor", listOf("§7Cerrar menú.")))

        player.openInventory(inv)
    }

    fun openShop(player: Player, level: Int) {
        val data = plugin.playerDataManager.getData(player.uniqueId) ?: return
        if (data.faction == Faction.NONE) return

        val safeLevel = level.coerceIn(1, 5)

        val holder = FactionHolder(FactionMenuType.SHOP, safeLevel)
        val inv = Bukkit.createInventory(holder, 54, "§8Tienda ${data.faction.displayName} - Nv.$safeLevel")
        val isLocked = data.factionLevel < safeLevel

        if (isLocked) {
            val lockItem = createButton(Material.BARRIER, "§c🔒 Nivel Insuficiente",
                listOf("§7Necesitas ser Nivel $safeLevel de Facción", "§7para comprar aquí.")
            )
            inv.setItem(22, lockItem)
        } else {
            val shopItems = plugin.shopManager.getItemsFor(data.faction, safeLevel)
            if (shopItems.isEmpty()) {
                inv.setItem(22, createButton(Material.BARRIER, "§cSin Stock", listOf("§7Esta tienda está vacía por ahora.")))
            } else {
                for (shopItem in shopItems) {
                    inv.setItem(shopItem.slot, shopItem.item)
                }
            }
        }

        if (safeLevel > 1) {
            inv.setItem(45, createButton(Material.ARROW, "§e◄ Nivel Anterior (${safeLevel - 1})", listOf("§7Volver a la tienda anterior")))
        }

        if (safeLevel < 5) {
            val nextLvl = safeLevel + 1
            val lockStatus = if (data.factionLevel >= nextLvl) "§a(Desbloqueado)" else "§c(Bloqueado)"
            inv.setItem(53, createButton(Material.ARROW, "§eNivel Siguiente ($nextLvl) ►", listOf(lockStatus)))
        }

        inv.setItem(49, createButton(Material.DARK_OAK_DOOR, "§7Volver al Menú", listOf()))

        player.openInventory(inv)
    }

    private fun createButton(mat: Material, name: String, lore: List<String>): ItemStack {
        val item = ItemStack(mat)
        val meta = item.itemMeta
        meta.setDisplayName(name)
        meta.lore = lore
        item.itemMeta = meta
        return item
    }
}

class FactionHolder(
    val type: FactionMenuType,
    val shopLevel: Int = 1,
    val faction: Faction = Faction.NONE
) : org.bukkit.inventory.InventoryHolder {
    override fun getInventory(): org.bukkit.inventory.Inventory = null!!
}

enum class FactionMenuType {
    SELECTION, DASHBOARD, SHOP, MISSIONS
}