package com.example.san.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.san.model.User

class AuthViewModel : ViewModel() {
    // Variable para la autenticación con email usando FirebaseAuth

    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage: StateFlow<String?> = _authMessage
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Variable para acceder a Firestore (base de datos)
    private val db = Firebase.firestore


    // MutableStateFlow para almacenar el usuario actual autenticado (puede ser null)
    private val _currentUser: MutableStateFlow<FirebaseUser?> = MutableStateFlow(auth.currentUser)

    // Estado público para observar el usuario actual (solo lectura)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    // MutableStateFlow para almacenar el UID del usuario actual (puede ser null)
    private val _uid = MutableStateFlow<String?>(auth.currentUser?.uid)

    // MutableStateFlow para indicar si un usuario está registrado (true o false)
    private val _isUserRegistered = MutableStateFlow(false)

    // Estado público para observar si el usuario está registrado (solo lectura)
    val isUserRegistered: StateFlow<Boolean> = _isUserRegistered

    //Funcion para getr de perfil
    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData
    //FUNCIONES DE REGISTRO DE USUARIO
    // Función para registrar un usuario con email y contraseña
    fun registerUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                // Crea el usuario en Firebase Auth
                auth.createUserWithEmailAndPassword(email, password).await()

                // Envía el correo de verificación
                auth.currentUser?.sendEmailVerification()?.addOnSuccessListener {

                    Log.d(javaClass.simpleName, "Correo de verificación enviado")
                }?.addOnFailureListener { e ->
                    Log.e(javaClass.simpleName, "Fallo al enviar correo de verificación: ${e.message}")
                }

                // Actualiza el estado
                _currentUser.value = auth.currentUser
                _isUserRegistered.value = true
                Log.d(javaClass.simpleName, "Usuario registrado correctamente")

            } catch (e: Exception) {
                _isUserRegistered.value = false
                Log.e(javaClass.simpleName, "Error al registrar usuario: ${e.message}")
            }
        }
    }


    // Función para iniciar sesión con email y contraseña
    fun loginUser(email: String, password: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                auth.currentUser?.reload()?.await()

                if (auth.currentUser?.isEmailVerified == true) {
                    _uid.value = auth.currentUser?.uid
                    _currentUser.value = auth.currentUser
                } else {
                    auth.signOut()
                    onError("Verifica tu correo antes de iniciar sesión")
                }
            } catch (e: Exception) {
                onError("Error al iniciar sesión: ${e.message}")
            }
        }
    }


    // Función para cerrar sesión del usuario actual
    fun logOut() {
        try {
            // Cierra sesión en Firebase Auth
            auth.signOut()
            // Limpia el estado del usuario y del registro
            _currentUser.value = null
            _isUserRegistered.value = false
            Log.d(javaClass.simpleName, "Sesión cerrada")
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error al cerrar sesión: ${e.message}")
        }
    }

    // Función para escuchar cambios en el estado de autenticación (login/logout)
    fun listenAuthState() {
        auth.addAuthStateListener { firebaseAuth: FirebaseAuth ->
            // Actualiza el usuario actual cuando cambia el estado
            _currentUser.value = firebaseAuth.currentUser
            // Actualiza el UID cuando cambia el estado
            _uid.value = firebaseAuth.currentUser?.uid
        }
    }
    fun saveUser(user: User, onSuccessMessage: (String) -> Unit) {
        db.collection("User")
            .document(user.uidUser) // Guarda usando el UID del usuario
            .set(user)
            .addOnSuccessListener {
                onSuccessMessage("Datos registrados con éxito")
            }
            .addOnFailureListener { e ->
                Log.e(javaClass.simpleName, "Error al guardar datos: ${e.message}")
            }
    }
    fun checkIfUserDataExists(uid: String, onResult: (Boolean) -> Unit) {
        db.collection("User").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("Firestore", "Datos del usuario ya existen")
                    onResult(true)
                } else {
                    Log.d("Firestore", "No hay datos del usuario")
                    onResult(false)
                }
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error al verificar datos: ${it.message}")
                onResult(false)
            }
    }
    fun clearAuthMessage() {
        _authMessage.value = null
    }
    fun checkIMC(uid: String, onSuccessMessage: (String) -> Unit, onFailure: (Exception) -> Unit) {


        db.collection("User").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val imc = document.getString("IMC") ?: "IMC no encontrado"
                    onSuccessMessage(imc)
                } else {
                    onSuccessMessage("Documento no existe")
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
    fun fetchUserData(uid: String) {
        viewModelScope.launch {
            try {
                val document = db.collection("User").document(uid).get().await()
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    _userData.value = user
                    Log.d("AuthViewModel", "Datos del usuario obtenidos")
                } else {
                    Log.d("AuthViewModel", "No se encontró el documento del usuario")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error al obtener datos del usuario: ${e.message}")
            }
        }
    }
    fun fetchCaloriesHistory(uid: String, onResult: (List<Pair<String, Int>>) -> Unit) {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("User")
                    .document(uid)
                    .collection("Calories")
                    .get()
                    .await()

                val history = snapshot.documents.mapNotNull { doc ->
                    val date = doc.id
                    val calories = doc.getLong("calories")?.toInt()
                    if (calories != null) Pair(date, calories) else null
                }.sortedByDescending { it.first }

                onResult(history)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error al obtener historial de calorías: ${e.message}")
                onResult(emptyList())
            }
        }
    }



}
