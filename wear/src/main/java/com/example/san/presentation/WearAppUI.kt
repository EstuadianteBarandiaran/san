package com.example.san.presentation

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

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
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (updateSuccess) {
                Text(
                    text = "✅ Calorías registradas correctamente",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(3000)
                    onClearSuccess()
                }
            }

            Button(
                onClick = onRequestIMC,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Obtener IMC")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onRequestCalories,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ingresar Calorías")
            }

            Spacer(modifier = Modifier.height(16.dp))

            imcValue?.let {
                Text(
                    text = "IMC: ${"%.2f".format(it)}",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = when {
                        it < 18.5 -> "Bajo peso"
                        it < 25 -> "Normal"
                        it < 30 -> "Sobrepeso"
                        else -> "Obesidad"
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            caloriesValue?.let {
                Text(
                    text = "Calorías: $it kcal",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
