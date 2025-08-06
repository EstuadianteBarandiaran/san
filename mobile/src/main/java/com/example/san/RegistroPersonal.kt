package com.example.san

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.san.network.RetrofitInstance
import com.example.san.viewmodel.AuthViewModel
import com.example.san.model.PesoEstatura
import com.example.san.model.ResultadoIMC
import com.example.san.model.User

class RegistroPersonal : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_personal)

        val btn: Button = findViewById(R.id.btnRegister)
        val edtName: EditText = findViewById(R.id.etNombre)
        val edtEdad: EditText = findViewById(R.id.etEdad)
        val edtAltura: EditText = findViewById(R.id.etEstatura)
        val edtPeso: EditText = findViewById(R.id.etPeso)
        val edtComida: EditText = findViewById(R.id.etComidas)
        val edtAgua: EditText = findViewById(R.id.etAgua)

        btn.setOnClickListener {
            val name = edtName.text.toString()
            val age = edtEdad.text.toString().toIntOrNull()
            val height = edtAltura.text.toString().toFloatOrNull()
            val weight = edtPeso.text.toString().toFloatOrNull()
            val meals = edtComida.text.toString().toIntOrNull()
            val waterLiters = edtAgua.text.toString().toFloatOrNull()

            if (name.isBlank() || age == null || weight == null || meals == null || waterLiters == null || height == null) {
                Toast.makeText(this, "Por favor completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = authViewModel.currentUser.value?.uid ?: "sin_uid"

            val user = User(
                uidUser = uid,
                Nombre = name,
                Edad = age,
                Estatura = height.toDouble(),
                Peso = weight.toInt(),
                CantidadComida = meals,
                CantidadLitros = waterLiters.toDouble()
            )

            val datos = PesoEstatura(peso = weight, estatura = height)

            authViewModel.saveUser(user) { mensaje ->
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            }

            authViewModel.checkIfUserDataExists(uid) { existe ->
                if (existe) {
                    RetrofitInstance.api.predecirIMC(datos).enqueue(object : retrofit2.Callback<ResultadoIMC> {
                        override fun onResponse(
                            call: retrofit2.Call<ResultadoIMC>,
                            response: retrofit2.Response<ResultadoIMC>
                        ) {
                            if (response.isSuccessful) {
                                val resultado = response.body()
                                val imcEstimado = resultado?.imc_estimado ?: 0f
                                Toast.makeText(this@RegistroPersonal, "IMC estimado: $imcEstimado", Toast.LENGTH_LONG).show()

                                val intent = Intent(this@RegistroPersonal, Home::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@RegistroPersonal, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: retrofit2.Call<ResultadoIMC>, t: Throwable) {
                            Log.d(javaClass.simpleName, "Error al conectar con el servidor: ${t.message}")
                            Toast.makeText(this@RegistroPersonal, "Error al conectar con el servidor: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    val intent = Intent(this@RegistroPersonal, Login::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
