package com.example.san.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn

class CaloriesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialCalories = intent.getIntExtra("INITIAL_CALORIES", 0)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme() // Mejora visual para Wear OS
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

    Scaffold { paddingValues ->
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // ✅ Corrige recortes en pantallas redondas
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(8.dp)
        ) {
            item {
                Text(
                    text = "Ingresar Calorías",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                OutlinedTextField(
                    value = caloriesInput,
                    onValueChange = { newValue ->
                        if (newValue.length <= 5) {
                            caloriesInput = newValue.filter { it.isDigit() }
                        }
                    },
                    label = { Text("Calorías (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            item {
                Text(
                    text = if (caloriesInput.isNotEmpty()) "$caloriesInput kcal" else "0 kcal",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Button(
                    onClick = {
                        val calories = caloriesInput.toIntOrNull() ?: 0
                        if (calories > 0) {
                            onSave(calories)
                        }
                    },
                    enabled = caloriesInput.isNotEmpty() && caloriesInput.toIntOrNull() ?: 0 > 0
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Guardar")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Guardar")
                }
            }

            item {
                Button(
                    onClick = {
                        if (caloriesInput.isNotEmpty()) {
                            caloriesInput = caloriesInput.dropLast(1)
                        }
                    },
                    enabled = caloriesInput.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Default.Backspace, contentDescription = "Borrar")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Borrar")
                }
            }
        }
    }
}
