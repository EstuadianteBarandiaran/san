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

import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.items
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.rememberScalingLazyListState

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
    val darkGreen = Color(0xFF142D2A)
    val mediumDarkGreen = Color(0xFF213B33)
    val mediumGreen = Color(0xFF365240)
    val lightGreen = Color(0xFF587153)
    val lightestGreen = Color(0xFF88996A)
    val white = Color(0xFFFFFFFF)

    val listState: ScalingLazyListState = rememberScalingLazyListState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(darkGreen)
    ) { padding ->
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(12.dp),
            state = listState
        ) {
            item {
                Text(
                    text = "Mi Salud",
                    color = lightestGreen,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (updateSuccess) {
                item {
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
            }

            item {
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
                        Text("Obtener IMC", fontSize = 14.sp)
                    }
                }
            }

            item {
                Button(
                    onClick = onRequestCalories,
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
                    Text("Ingresar Calorías", fontSize = 14.sp)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
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
                                        text = "IMC: ${"%.2f".format(it)}",
                                        color = white,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = when {
                                            it < 18.5 -> "Bajo peso"
                                            it < 25 -> "Normal"
                                            it < 30 -> "Sobrepeso"
                                            else -> "Obesidad"
                                        },
                                        color = when {
                                            it < 18.5 -> Color(0xFFFFA726)
                                            it < 25 -> lightestGreen
                                            it < 30 -> Color(0xFFFF7043)
                                            else -> Color(0xFFEF5350)
                                        },
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            caloriesValue?.let {
                                Text(
                                    text = "Calorías: $it kcal",
                                    color = white,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Presiona los botones para ver tus datos",
                        color = lightestGreen,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            errorMessage?.let {
                item {
                    Text(
                        text = it,
                        color = Color(0xFFEF5350),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            item {
                Text(
                    text = "San App",
                    color = lightestGreen.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
