package org.ReDiego0.bastionCore.utils

import org.ReDiego0.bastionCore.BastionCore
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.io.File

object ContractUtils {
    val MISSION_TYPE_KEY = NamespacedKey(BastionCore.instance, "mission_type")
    val MISSION_ID_KEY = NamespacedKey(BastionCore.instance, "mission_id")

    val DATA_WORLD_KEY = NamespacedKey(BastionCore.instance, "proc_world")
    val DATA_BOSS_KEY = NamespacedKey(BastionCore.instance, "proc_boss")
    val DATA_REWARD_KEY = NamespacedKey(BastionCore.instance, "proc_reward")
    val DATA_TRIGGER_KEY = NamespacedKey(BastionCore.instance, "proc_trigger")
    val DATA_THREAT_KEY = NamespacedKey(BastionCore.instance, "proc_threat")
    val EXPIRATION_KEY = NamespacedKey(BastionCore.instance, "expiration_time")
    val DATA_MISSION_TYPE_ENUM_KEY = NamespacedKey(BastionCore.instance, "proc_type_enum")
    val DATA_REQUIRED_AMOUNT_KEY = NamespacedKey(BastionCore.instance, "proc_amount")
    val DATA_SPAWN_RADIUS_KEY = NamespacedKey(BastionCore.instance, "proc_radius")


    private var missionsConfig: YamlConfiguration? = null

    fun reloadMissions() {
        val file = File(BastionCore.instance.dataFolder, "missions.yml")
        if (!file.exists()) {
            BastionCore.instance.saveResource("missions.yml", false)
        }
        missionsConfig = YamlConfiguration.loadConfiguration(file)
    }

    fun getConfig(): YamlConfiguration {
        if (missionsConfig == null) reloadMissions()
        return missionsConfig!!
    }

    fun createMissionItem(missionId: String): ItemStack? {
        val config = getConfig()
        val path = "missions.$missionId"

        if (!config.contains(path)) return null

        val matName = config.getString("$path.item.material", "PAPER")!!
        val material = Material.getMaterial(matName) ?: Material.PAPER

        val item = ItemStack(material)
        val meta = item.itemMeta

        meta.setDisplayName(config.getString("$path.display_name")?.replace("&", "§"))

        val lore = config.getStringList("$path.lore").map { it.replace("&", "§") }
        meta.lore = lore

        if (config.getBoolean("$path.item.glow")) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }

        meta.persistentDataContainer.set(MISSION_ID_KEY, PersistentDataType.STRING, missionId)

        item.itemMeta = meta
        return item
    }

    fun getMissionId(item: ItemStack?): String? {
        if (item == null || !item.hasItemMeta()) return null
        return item.itemMeta.persistentDataContainer.get(MISSION_ID_KEY, PersistentDataType.STRING)
    }

    fun stampExpiration(item: ItemStack): ItemStack {
        val meta = item.itemMeta ?: return item
        val minutes = BastionCore.instance.config.getInt("settings.contract_expiration_minutes", 45)

        val expiryDate = System.currentTimeMillis() + (minutes * 60 * 1000)

        meta.persistentDataContainer.set(EXPIRATION_KEY, PersistentDataType.LONG, expiryDate)

        val lore = meta.lore ?: ArrayList()
        lore.add("")
        lore.add("§c⚠ Expira en: $minutes min")
        lore.add("§8(El tiempo corre tras aceptarlo)")
        meta.lore = lore

        item.itemMeta = meta
        return item
    }

    fun isExpired(item: ItemStack): Boolean {
        if (!item.hasItemMeta()) return false
        val pdc = item.itemMeta.persistentDataContainer

        if (!pdc.has(EXPIRATION_KEY, PersistentDataType.LONG)) return false // No tiene fecha = No expira (Especiales)

        val expiryTime = pdc.get(EXPIRATION_KEY, PersistentDataType.LONG) ?: 0L
        return System.currentTimeMillis() > expiryTime
    }
}