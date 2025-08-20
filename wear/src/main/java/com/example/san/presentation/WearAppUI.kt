package com.example.san.presentation

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WearAppUI(
    context: Context,
    imcValue: Double?,
    caloriesValue: Int?,
    isLoading: Boolean,
    errorMessage: String?,
    updateSuccess: Boolean,
    onClearSuccess: () -> Unit,
    onClearError: () -> Unit,
    onRequestIMC: () -> Unit,
    onRequestCalories: () -> Unit
) {
    // Paleta de colores de tu app
    val darkGreen = Color(0xFF142D2A)
    val mediumDarkGreen = Color(0xFF213B33)
    val mediumGreen = Color(0xFF365240)
    val lightGreen = Color(0xFF587153)
    val lightestGreen = Color(0xFF88996A)
    val white = Color(0xFFFFFFFF)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(darkGreen)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Header
            Text(
                text = "Mi Salud",
                color = lightestGreen, // Cambiado para mejor visibilidad
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            if (updateSuccess) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(lightGreen, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Éxito",
                            tint = white,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Calorías registradas",
                            color = white,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(3000)
                    onClearSuccess()
                }
            }

            // SOLO DOS BOTONES - ELIMINADO CUALQUIER BOTÓN EXTRA
            // Botón IMC
            Button(
                onClick = onRequestIMC,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = mediumGreen,
                    contentColor = white
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = white,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MonitorWeight,
                        contentDescription = "IMC",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Obtener IMC",
                        fontSize = 14.sp
                    )
                }
            }

            // Botón Calorías
            Button(
                onClick = onRequestCalories,
                enabled = !isLoading, // Agregado para deshabilitar durante carga
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = lightGreen,
                    contentColor = white
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Fastfood,
                    contentDescription = "Calorías",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ingresar Calorías",
                    fontSize = 14.sp
                )
            }

            // Sección de resultados
            if (imcValue != null || caloriesValue != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(mediumDarkGreen, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        imcValue?.let {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "📊 IMC",
                                    color = lightestGreen,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "%.1f".format(it),
                                    color = white,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = when {
                                        it < 18.5 -> "Bajo peso"
                                        it < 25 -> "Normal ✅"
                                        it < 30 -> "Sobrepeso"
                                        else -> "Obesidad"
                                    },
                                    color = when {
                                        it < 18.5 -> Color(0xFFFFA726)
                                        it < 25 -> lightestGreen
                                        it < 30 -> Color(0xFFFF7043)
                                        else -> Color(0xFFEF5350)
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        caloriesValue?.let {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "🔥 CALORÍAS",
                                    color = lightestGreen,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$it kcal",
                                    color = white,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Presiona los botones para ver tus datos",
                    color = lightestGreen,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            errorMessage?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x44EF5350), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = it,
                        color = Color(0xFFEF5350),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                LaunchedEffect(it) {
                    kotlinx.coroutines.delay(5000)
                    onClearError()
                }
            }

            // Footer
            Text(
                text = "San App",
                color = lightestGreen,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}