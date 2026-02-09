package org.ReDiego0.bastionCore.combat

enum class Role(
    val id: String,
    val displayName: String,
    val description: String,
    val baseHealth: Double
) {
    RECLUTA(
        "recluta",
        "Recluta",
        "Personal básico sin especialización.",
        20.0
    ),

    VANGUARDIA(
        "vanguardia",
        "Vanguardia",
        "Especialista en defensa y control de masas. La muralla del equipo.",
        26.0
    ),

    RASTREADOR(
        "rastreador",
        "Rastreador",
        "Especialista en rango y trampas. Mantienen la presión a distancia.",
        20.0
    ),

    CENTINELA(
        "centinela",
        "Centinela",
        "Especialista en daño explosivo y movilidad. Alto riesgo, alta recompensa.",
        18.0
    );

    companion object {
        // obtener un rol desde un String
        fun fromId(id: String): Role {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: RECLUTA
        }
    }
}