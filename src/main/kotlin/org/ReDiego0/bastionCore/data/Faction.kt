package org.ReDiego0.bastionCore.data

import org.bukkit.Material

enum class Faction(
    val id: String,
    val displayName: String,
    val color: String,
    val description: String,
    val icon: Material
) {
    NONE(
        "none",
        "Sin Facción",
        "§7",
        "Un mercenario sin bandera.",
        Material.GRAY_DYE
    ),

    AKAI_REN(
        "akai",
        "Loto Carmesí",
        "§c",
        "La fuerza es la única verdad. Conquista a través de la sangre.",
        Material.RED_DYE
    ),

    AOI_TSUKI(
        "aoi",
        "Luna Azur",
        "§b",
        "La calma antes de la tormenta. Protección y sabiduría ancestral.",
        Material.BLUE_DYE
    );

    companion object {
        fun fromId(id: String): Faction {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: NONE
        }
    }
}