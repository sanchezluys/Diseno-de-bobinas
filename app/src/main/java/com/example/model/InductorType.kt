package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Transform
import androidx.compose.ui.graphics.vector.ImageVector

enum class InductorType(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    CILINDRICO(
        title = "Cilíndrico",
        description = "Solenoide de una o varias capas sobre forma cilíndrica",
        icon = Icons.Default.Loop
    ),
    TOROIDAL(
        title = "Toroidal",
        description = "Bobina enrollada en núcleo de anillo cerrado tipo dona",
        icon = Icons.Default.Circle
    ),
    ANTENA(
        title = "Antena de cuadro",
        description = "Espira circular o loop para recepción/transmisión RF",
        icon = Icons.Default.Radio
    ),
    TRANSFORMADOR(
        title = "Transformador",
        description = "Dos devanados acoplados por núcleo magnético común",
        icon = Icons.Default.Transform
    )
}
