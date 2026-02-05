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

            return when {
                id.contains("greatsword") || id.contains("gran_espada") -> WeaponType.GREATSWORD
                id.contains("hammer") || id.contains("martillo") -> WeaponType.HAMMER
                id.contains("spear") || id.contains("lanza") -> WeaponType.SPEAR
                id.contains("katana") -> WeaponType.KATANA
                id.contains("dagger") || id.contains("daga") -> WeaponType.DUAL_BLADES
                id.contains("bow") || id.contains("arco") -> WeaponType.BOW
                else -> WeaponType.NONE
            }
        }

        val name = meta.displayName.lowercase()

        return when {
            name.contains("gran espada") -> WeaponType.GREATSWORD
            name.contains("lanza") -> WeaponType.SPEAR
            name.contains("martillo") -> WeaponType.HAMMER
            name.contains("katana") -> WeaponType.KATANA
            name.contains("dagas") -> WeaponType.DUAL_BLADES
            name.contains("arco") -> WeaponType.BOW

            item.type.name.contains("NETHERITE_SWORD") -> WeaponType.GREATSWORD
            item.type.name.contains("TRIDENT") -> WeaponType.SPEAR
            item.type.name.contains("MACE") -> WeaponType.HAMMER

            else -> WeaponType.NONE
        }
    }
}