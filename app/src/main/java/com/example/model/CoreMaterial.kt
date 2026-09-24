package com.example.model

enum class CoreMaterial(
    val displayName: String,
    val relativePermeability: Double, // μr
    val description: String
) {
    AIRE(
        displayName = "Aire (Sin núcleo)",
        relativePermeability = 1.0,
        description = "μr = 1.0 — No sufre saturación ni pérdidas por histéresis."
    ),
    FERRITA(
        displayName = "Ferrita",
        relativePermeability = 2000.0,
        description = "μr ≈ 2000 — Óptimo para altas frecuencias (RF y fuentes conmutadas)."
    ),
    HIERRO(
        displayName = "Hierro",
        relativePermeability = 200.0,
        description = "μr ≈ 200 — Común en aplicaciones de baja frecuencia y transformadores de potencia."
    )
}
