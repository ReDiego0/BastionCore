package org.ReDiego0.bastionCore.combat

enum class WeaponType(val displayName: String) {
    NONE("Desarmado"),

    // Pesadas
    GREATSWORD("Gran Espada"),
    HAMMER("Martillo de Guerra"),

    // Ligeras / Técnicas
    KATANA("Espada Larga"),
    DUAL_BLADES("Dagas Duales"),

    // Rango / Asta
    SPEAR("Lanza"),
    BOW("Arco Pesado");
}