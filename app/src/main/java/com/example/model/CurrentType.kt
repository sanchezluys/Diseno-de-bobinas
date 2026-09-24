package com.example.model

enum class CurrentType(val label: String, val description: String) {
    DC(
        label = "DC (Continua)",
        description = "Corriente constante. La bobina solo presenta resistencia óhmica pura."
    ),
    AC(
        label = "AC (Alterna)",
        description = "Corriente variable. La bobina presenta reactancia inductiva (Xl) e impedancia (Z)."
    )
}
