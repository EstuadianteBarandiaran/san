package com.example.san.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn

class CaloriesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialCalories = intent.getIntExtra("INITIAL_CALORIES", 0)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme() // Tema oscuro uniforme
            ) {
                CaloriesInputScreen(
                    initialCalories = initialCalories,
                    onSave = { calories ->
                        val resultIntent = intent.apply {
                            putExtra("CALORIES_RESULT", calories)
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun CaloriesInputScreen(
    initialCalories: Int = 0,
    onSave: (Int) -> Unit
) {
    var caloriesInput by remember { mutableStateOf(initialCalories.toString()) }

    // colores a usar en la APPPP AAJgGGg
    val darkGreen = Color(0xFF142D2A)
    val mediumGreen = Color(0xFF365240)
    val lightGreen = Color(0xFF587153)
    val lightestGreen = Color(0xFF88996A)
    val white = Color(0xFFFFFFFF)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(darkGreen)
    ) { paddingValues ->
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(8.dp)
        ) {
            // Título
            item {
                Text(
                    text = "Ingresar Calorías",
                    color = lightestGreen,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Campo de texto
            item {
                OutlinedTextField(
                    value = caloriesInput,
                    onValueChange = { newValue ->
                        if (newValue.length <= 5) {
                            caloriesInput = newValue.filter { it.isDigit() }
                        }
                    },
                    label = { Text("Calorías (kcal)", color = lightestGreen) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = white,
                        unfocusedTextColor = white,
                        focusedBorderColor = lightGreen,
                        unfocusedBorderColor = lightestGreen,
                        cursorColor = lightGreen,
                        focusedLabelColor = lightGreen,
                        unfocusedLabelColor = lightestGreen
                    )
                )
            }

            // Resultado
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(mediumGreen, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (caloriesInput.isNotEmpty()) "${caloriesInput} kcal" else "0 kcal",
                        color = white,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Botón Guardar
            item {
                Button(
                    onClick = {
                        val calories = caloriesInput.toIntOrNull() ?: 0
                        if (calories > 0) {
                            onSave(calories)
                        }
                    },
                    enabled = caloriesInput.isNotEmpty() && caloriesInput.toIntOrNull() ?: 0 > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = lightGreen,
                        contentColor = white
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Guardar")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Guardar")
                }
            }

            // Botón Borrar
            item {
                Button(
                    onClick = {
                        if (caloriesInput.isNotEmpty()) {
                            caloriesInput = caloriesInput.dropLast(1)
                        }
                    },
                    enabled = caloriesInput.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF5350),
                        contentColor = white
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Backspace, contentDescription = "Borrar")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Borrar")
                }
            }

            // Footer
            item {
                Text(
                    text = "San App",
                    color = lightestGreen,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
