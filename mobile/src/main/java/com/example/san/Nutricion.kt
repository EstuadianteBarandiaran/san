package com.example.san

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class Nutricion: AppCompatActivity() {
    private lateinit var result: TextView
    private lateinit var confidence: TextView
    private lateinit var caloriesText: TextView
    private lateinit var imageView: ImageView
    private lateinit var picture: Button
    private val imageSize = 224
    private lateinit var tflite: Interpreter

    // Clases EXACTAMENTE como están en Teachable Machine (según tus labels)
    private val modelClasses = arrayOf(
        "cebolla",      // 0
        "tomate",       // 1
        "jalapeno",     // 2
        "cocacola",     // 3
        "manzana",      // 4
        "tomatejalapeno", // 5
        "cebojalatoma", // 6
        "desayunodcampeon", // 7
        "cigarro"       // 8
    )

    // Nombres para mostrar (puedes personalizarlos)
    private val displayNames = mapOf(
        "cebolla" to "Cebolla",
        "tomate" to "Tomate",
        "jalapeno" to "Jalapeño",
        "cocacola" to "Coca-Cola",
        "manzana" to "Manzana",
        "tomatejalapeno" to "Tomate + Jalapeño",
        "cebojalatoma" to "Cebolla + Jalapeño + Tomate",
        "desayunodcampeon" to "Desayuno de Campeones",
        "cigarro" to "Cigarro"
    )

    // Base de calorías (ajustada a tus requerimientos)
    private val caloriesDatabase = mapOf(
        "cebolla" to 40,
        "tomate" to 18,
        "jalapeno" to 29,
        "cocacola" to 140,
        "manzana" to 52,
        "tomatejalapeno" to 47,  // 18+29
        "cebojalatoma" to 87,    // 40+29+18
        "desayunodcampeon" to 0,
        "cigarro" to 0
    )

    private val confidenceThreshold = 0.5f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vista_camara)

        // Inicialización de vistas
        result = findViewById(R.id.result)
        confidence = findViewById(R.id.confidence)
        caloriesText = findViewById(R.id.caloriesText)
        imageView = findViewById(R.id.imageView)
        picture = findViewById(R.id.btnregister)

        // Cargar modelo
        try {
            tflite = Interpreter(loadModelFile(this, "model.tflite"))
            Log.d("Model", "Modelo cargado correctamente")
            Log.d("Model", "Clases del modelo: ${modelClasses.joinToString()}")
        } catch (e: IOException) {
            Log.e("Model", "Error al cargar modelo", e)
            result.text = "Error al cargar modelo"
        }

        picture.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, 1)
            } else {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
            }
        }
    }

    private fun loadModelFile(context: Context, modelFile: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelFile)
        FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
            return inputStream.channel.map(
                FileChannel.MapMode.READ_ONLY,
                fileDescriptor.startOffset,
                fileDescriptor.declaredLength
            )
        }
    }

    private fun classifyImage(image: Bitmap) {
        try {
            // Preprocesamiento de imagen
            val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3).apply {
                order(ByteOrder.nativeOrder())
                val intValues = IntArray(imageSize * imageSize)
                image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)

                for (pixelValue in intValues) {
                    putFloat(((pixelValue shr 16 and 0xFF) / 255.0f))
                    putFloat(((pixelValue shr 8 and 0xFF) / 255.0f))
                    putFloat(((pixelValue and 0xFF) / 255.0f))
                }
            }

            // Configurar buffers de entrada/salida
            val inputBuffer = TensorBuffer.createFixedSize(
                intArrayOf(1, imageSize, imageSize, 3),
                org.tensorflow.lite.DataType.FLOAT32
            ).apply { loadBuffer(byteBuffer) }

            val outputBuffer = TensorBuffer.createFixedSize(
                intArrayOf(1, modelClasses.size),
                org.tensorflow.lite.DataType.FLOAT32
            )

            // Ejecutar inferencia
            tflite.run(inputBuffer.buffer, outputBuffer.buffer)

            // Procesar resultados
            processOutput(outputBuffer.floatArray)

        } catch (e: Exception) {
            Log.e("Classification", "Error: ${e.message}")
            result.text = "Error en clasificación"
            confidence.text = e.message?.take(100) ?: "Error desconocido"
        }
    }

    private fun processOutput(confidences: FloatArray) {
        val results = mutableListOf<DetectionResult>().apply {
            for (i in confidences.indices) {
                if (confidences[i] > confidenceThreshold) {
                    val className = modelClasses[i]
                    add(DetectionResult(
                        displayName = displayNames[className] ?: className,
                        originalName = className,
                        confidence = confidences[i],
                        classIndex = i  // Añadimos el índice para debug
                    ))
                }
            }
            sortByDescending { it.confidence }
        }

        // Debug: Mostrar confianzas completas
        Log.d("Classification", "Confianzas completas:")
        confidences.forEachIndexed { index, conf ->
            Log.d("Classification", "${modelClasses[index]}: ${"%.2f".format(conf)}")
        }

        if (results.isNotEmpty()) {
            val detectedText = StringBuilder("Detectado:\n")
            val confidenceText = StringBuilder("Confianza:\n")
            var totalCalories = 0

            results.forEach { item ->
                detectedText.append("• ${item.displayName}\n")
                confidenceText.append("%.1f%%\n".format(item.confidence * 100))
                totalCalories += calculateCalories(item)
                Log.d("Classification", "Detectado: ${item.displayName} (${item.originalName}, idx:${item.classIndex})")
            }

            runOnUiThread {
                result.text = detectedText
                confidence.text = confidenceText
                caloriesText.text = "Calorías totales: $totalCalories kcal"
            }
        } else {
            runOnUiThread {
                result.text = "No se detectaron ingredientes"
                confidence.text = ""
                caloriesText.text = ""
            }
        }
    }

    private fun calculateCalories(item: DetectionResult): Int {
        return when (item.originalName) {
            "cigarro", "desayunodcampeon" -> 0
            else -> (caloriesDatabase[item.originalName] ?: 0).times(
                when {
                    item.confidence > 0.8f -> 1.0f
                    item.confidence > 0.6f -> 0.75f
                    else -> 0.5f
                }
            ).toInt()
        }
    }

    data class DetectionResult(
        val displayName: String,
        val originalName: String,
        val confidence: Float,
        val classIndex: Int = -1  // Para debug
    )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            (data?.extras?.get("data") as? Bitmap)?.let { originalImage ->
                try {
                    val dimension = minOf(originalImage.width, originalImage.height)
                    val thumbnail = ThumbnailUtils.extractThumbnail(
                        originalImage,
                        dimension,
                        dimension
                    )
                    imageView.setImageBitmap(thumbnail)
                    classifyImage(Bitmap.createScaledBitmap(thumbnail, imageSize, imageSize, false))
                } catch (e: Exception) {
                    Log.e("Camera", "Error processing image", e)
                    result.text = "Error al procesar imagen"
                }
            } ?: run {
                result.text = "No se obtuvo imagen válida"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tflite.close()
    }
}