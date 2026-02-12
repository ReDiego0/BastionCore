package org.ReDiego0.bastionCore.combat

enum class Role(
    val id: String,
    val displayName: String,
    val description: String,
    val baseHealth: Double
) {
    VAGABUNDO(
        "vagabundo",
        "Vagabundo",
        "Un alma errante sin propósito definido aún.",
        20.0
    ),

    SAMURAI(
        "samurai",
        "Samurái",
        "El baluarte inquebrantable. Especialista en mitigar daño y proteger aliados.",
        50.0
    ),

    ORACULO(
        "oraculo",
        "Oráculo",
        "El nexo espiritual. Especialista en sanación y manipulación del flujo de batalla.",
        40.0
    ),

    KENSAI(
        "kensai",
        "Kensai",
        "Ha trascendido el uso de la armadura para enfocarse en la velocidad y la técnica perfecta.",
        30.0
    );

    companion object {
        fun fromId(id: String): Role {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: VAGABUNDO
        }
    }
}