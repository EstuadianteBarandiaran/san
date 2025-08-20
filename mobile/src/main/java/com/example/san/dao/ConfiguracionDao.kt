package com.example.san.dao
import androidx.room.*
import com.example.san.model.Configuracion

@Dao
interface ConfiguracionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(config: Configuracion)

    @Query("SELECT valor FROM configuraciones WHERE clave = :clave LIMIT 1")
    suspend fun obtenerValor(clave: String): String?

    @Delete
    suspend fun eliminar(config: Configuracion)

    @Query("DELETE FROM configuraciones")
    suspend fun eliminarTodo()

    @Query("SELECT * FROM configuraciones WHERE clave LIKE 'AlarmaEstado%' AND valor = 'true'")
    suspend fun obtenerAlarmasActivas(): List<Configuracion>


}