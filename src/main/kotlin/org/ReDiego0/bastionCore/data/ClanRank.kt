package org.ReDiego0.bastionCore.data

enum class ClanRank(val level: Int, val defaultName: String) {
    ASHIGARU(1, "Ashigaru"),   // Recluta / Carne de cañón
    BUSHI(2, "Bushi"),         // Guerrero / Miembro estándar
    KASHIRA(3, "Kashira"),     // Capitán / Oficial
    SANBO(4, "Sanbo"),         // Estratega / Mano derecha
    TAISHO(5, "Taisho");       // Comandante / Dueño

    companion object {
        fun fromLevel(level: Int): ClanRank {
            return entries.find { it.level == level } ?: ASHIGARU
        }
    }
}