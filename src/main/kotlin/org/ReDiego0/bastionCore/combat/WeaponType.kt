package org.ReDiego0.bastionCore.combat

enum class WeaponType(val displayName: String, val id: String) {
    NONE("Desarmado", "none"),

    // Pesadas
    GREATSWORD("Gran Espada", "greatsword"),
    HAMMER("Martillo", "hammer"),

    // Ligeras
    KATANA("Katana", "katana"),
    DUAL_BLADES("Dagas Duales", "dual_blades"),

    // Rango
    SPEAR("Lanza", "spear"),
    BOW("Arco Pesado", "bow");

    companion object {
        // detectar el arma según NBT
        fun fromId(id: String): WeaponType {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: NONE
        }
    }
}