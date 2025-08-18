package com.example.san

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.san.viewmodel.AuthViewModel
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope


class Register : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        val button: Button = findViewById(R.id.btnCircle)
        val btnl: Button = findViewById(R.id.btn_iniciar)
        val user=findViewById<EditText>(R.id.etEmail)
        val password=findViewById<EditText>(R.id.etPassword)
        val password1=findViewById<EditText>(R.id.etPassword1)

        button.setOnClickListener {
            //Nombre de las variables con el edittext
            val user1:String=user.text.toString()
            val pass:String=password.text.toString()
            val pass1:String=password1.text.toString()

            //Datos para evaluar el numero
            val hasNumber= pass.any{it.isDigit()}

            //Verificar que tenga un caracter especial
            val specialChar="!@#$%^&*()-_=+<>?/{}~|"
            val hasSpecial= pass.any{it in specialChar}

            // Verificar que no tenga espacios en blanco
            if (user1.isBlank() or pass.isBlank() or pass1.isBlank() ){
                Toast.makeText(this, "Los datos están en blanco", Toast.LENGTH_SHORT).show()
            //Verificar que tenga mayor a 8 cifras
            }else if (!Patterns.EMAIL_ADDRESS.matcher(user1).matches()) {
                Toast.makeText(this, "Correo no válido", Toast.LENGTH_SHORT).show()}
            else if(pass != pass1){
                Toast.makeText(this, "La clave esta incorrecta en alguno de los dos campos", Toast.LENGTH_SHORT).show()
            }
            else if (pass.length <8){
                Toast.makeText(this, "Estan muy corta la clave", Toast.LENGTH_SHORT).show()
            }else if(!hasNumber) {
                Toast.makeText(this, "La contraseña debe tener al menos un número", Toast.LENGTH_SHORT).show()
            } else if (!hasSpecial) {
            Toast.makeText(this, "La contraseña debe tener al menos un signo especial", Toast.LENGTH_SHORT).show()
            } else{
                authViewModel.registerUser(user1, pass)
                Toast.makeText(this, "Registrando usuario...", Toast.LENGTH_SHORT).show()
            }
        }
        btnl.setOnClickListener {
            val intent= Intent(this, MainActivity::class.java)
            startActivity(intent)
            }
        lifecycleScope.launchWhenStarted {
            authViewModel.isUserRegistered.collect { isRegistered ->
                if (isRegistered) {

                    val intent = Intent(this@Register, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    Toast.makeText(this@Register, "Error al registrar usuario", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }


}