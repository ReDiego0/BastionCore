package org.ReDiego0.bastionCore.combat

enum class WeaponType(val displayName: String, val id: String) {
    NONE("Desarmado", "none"),

    KATANA("Katana", "katana"),
    NODACHI("Nodachi", "nodachi"),

    YUMI("Yumi", "yumi"),
    NAGINATA("Naginata", "naginata"),

    TEKKO("Tekko", "tekko"),
    KAMA("Kama", "kama");

    companion object {
        fun fromId(id: String): WeaponType {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: NONE
        }
    }
}