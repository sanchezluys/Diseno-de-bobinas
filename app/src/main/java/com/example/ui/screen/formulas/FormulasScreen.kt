package com.example.ui.screen.formulas

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulasScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBack)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Fórmulas y Teoría",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("formulas_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Intro educational note
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Guía Técnica para Estudiantes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "A continuación se detallan los principios físicos y modelos matemáticos empleados por la aplicación para calcular la inductancia, resistencia e impedancia de las bobinas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            FormulaCard(
                title = "1. Resistencia Óhmica DC",
                formula = "R = ρ · L / A",
                variables = listOf(
                    "ρ (Cobre a 20°C)" to "1.68 × 10⁻⁸ Ω·m",
                    "L (Longitud conductor)" to "N × (π · d_bobina)",
                    "A (Área sección cable)" to "π · (d_cable / 2)²"
                ),
                explanation = "Representa la oposición al flujo de electrones en el alambre de cobre debido a su resistividad natural."
            )

            FormulaCard(
                title = "2. Inductancia Cilíndrica (Fórmula de Wheeler)",
                formula = "L = (μ₀ · μᵣ · N² · π · r²) / (l + 0.9 · r)",
                variables = listOf(
                    "μ₀ (Permeabilidad vacío)" to "4π × 10⁻⁷ H/m ≈ 1.257 × 10⁻⁶ H/m",
                    "μᵣ (Permeabilidad relativa)" to "Aire: 1.0 | Ferrita: ~2000 | Hierro: ~200",
                    "N" to "Número de espiras",
                    "r" to "Radio de la bobina (m)",
                    "l" to "Longitud axial de la bobina (N × d_cable)"
                ),
                explanation = "Fórmula clásica empírica de Wheeler para solenoides monocapa, con precisión de ~1% para l > 0.8 r."
            )

            FormulaCard(
                title = "3. Inductancia Toroidal",
                formula = "L = (μ₀ · μᵣ · N² · A_core) / (2 · π · r_medio)",
                variables = listOf(
                    "A_core" to "Área de la sección transversal del núcleo circular",
                    "r_medio" to "Radio medio del anillo toroidal"
                ),
                explanation = "En un toroide, las líneas de flujo magnético quedan casi completamente confinadas en el núcleo cerrado, minimizando la radiación externa y el acoplamiento parásito."
            )

            FormulaCard(
                title = "4. Reactancia e Impedancia AC",
                formula = "X_L = 2π · f · L\nZ = √(R² + X_L²)",
                variables = listOf(
                    "f" to "Frecuencia de la señal alterna (Hz)",
                    "X_L" to "Reactancia inductiva en Ohmios (Ω)",
                    "Z" to "Impedancia total de la bobina (Ω)"
                ),
                explanation = "En régimen AC senoidal, la reactancia inductiva se incrementa linealmente con la frecuencia, limitando la corriente alterna según I = V / Z."
            )

            FormulaCard(
                title = "5. Flujo Magnético y Ley de Faraday",
                formula = "Φ = (L · I) / N\nB = Φ / A_core",
                variables = listOf(
                    "Φ" to "Flujo magnético en Webers (Wb)",
                    "B" to "Densidad de flujo magnético en Teslas (T)",
                    "W" to "Energía almacenada: ½ · L · I² (Joules)"
                ),
                explanation = "El flujo magnético es proporcional a la corriente inducida y al número de enlaces de flujo magnético por vuelta."
            )
        }
    }
}

@Composable
private fun FormulaCard(
    title: String,
    formula: String,
    variables: List<Pair<String, String>>,
    explanation: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Functions,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Formula box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = formula,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Variables list
            variables.forEach { (name, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
