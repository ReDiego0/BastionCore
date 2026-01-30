package org.ReDiego0.bastionCore.utils

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

        val container = item.itemMeta.persistentDataContainer
        if (!container.has(WEAPON_KEY, PersistentDataType.STRING)) return WeaponType.NONE

        val typeName = container.get(WEAPON_KEY, PersistentDataType.STRING)
        return try {
            WeaponType.valueOf(typeName!!)
        } catch (e: IllegalArgumentException) {
            WeaponType.NONE
        }
    }
}