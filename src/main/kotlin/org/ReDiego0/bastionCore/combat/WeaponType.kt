package org.ReDiego0.bastionCore.combat

enum class WeaponType(val displayName: String, val id: String) {
    NONE("Desarmado", "none"),

    GREATSWORD("Gran Espada", "greatsword"),
    HAMMER("Martillo", "hammer"),

    KATANA("Katana", "katana"),
    DUAL_BLADES("Dagas Duales", "dual_blades"),

    SPEAR("Lanza", "spear"),
    BOW("Arco Pesado", "bow");

    companion object {
        //  el arma según NBT
        fun fromId(id: String): WeaponType {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: NONE
        }
    }
}