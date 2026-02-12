package org.ReDiego0.bastionCore.utils

import dev.lone.itemsadder.api.CustomStack
import org.ReDiego0.bastionCore.BastionCore
import org.ReDiego0.bastionCore.combat.WeaponType
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object ItemTags {

    private val WEAPON_KEY = NamespacedKey(BastionCore.instance, "weapon_type")

    fun setWeaponType(item: ItemStack, type: WeaponType): ItemStack {
        if (!item.hasItemMeta()) return item
        val meta = item.itemMeta
        meta.persistentDataContainer.set(WEAPON_KEY, PersistentDataType.STRING, type.name)
        item.itemMeta = meta
        return item
    }

    fun getWeaponType(item: ItemStack?): WeaponType {
        if (item == null || !item.hasItemMeta()) return WeaponType.NONE

        val meta = item.itemMeta
        val pdc = meta.persistentDataContainer

        if (pdc.has(WEAPON_KEY, PersistentDataType.STRING)) {
            val typeStr = pdc.get(WEAPON_KEY, PersistentDataType.STRING)
            return try {
                WeaponType.valueOf(typeStr!!)
            } catch (e: IllegalArgumentException) {
                WeaponType.NONE
            }
        }

        val customStack = CustomStack.byItemStack(item)
        if (customStack != null) {
            val id = customStack.namespacedID.lowercase()
            return matchString(id)
        }

        val name = meta.displayName.lowercase()
        return matchString(name)
    }

    private fun matchString(input: String): WeaponType {
        return when {
            input.contains("nodachi") -> WeaponType.NODACHI
            input.contains("katana") -> WeaponType.KATANA

            input.contains("yumi") || input.contains("arco") -> WeaponType.YUMI
            input.contains("naginata") || input.contains("lanza") -> WeaponType.NAGINATA

            input.contains("tekko") || input.contains("puño") || input.contains("garras") -> WeaponType.TEKKO
            input.contains("kama") || input.contains("hoz") -> WeaponType.KAMA

            else -> WeaponType.NONE
        }
    }
}