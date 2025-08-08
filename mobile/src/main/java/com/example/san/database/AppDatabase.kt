package com.example.san.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.san.dao.ConfiguracionDao
import com.example.san.model.Configuracion
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Configuracion::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun configuracionDao(): ConfiguracionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mi_base_datos"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getDatabase(context).configuracionDao()
                                dao.insertar(Configuracion(clave="AlarmaHora1", valor="7"))
                                dao.insertar(Configuracion(clave="AlarmaMinutos1", valor="0"))
                                dao.insertar(Configuracion(clave="AlarmaEstado1", valor="true"))

                                dao.insertar(Configuracion(clave="AlarmaHora2", valor="10"))
                                dao.insertar(Configuracion(clave="AlarmaMinutos2", valor="30"))
                                dao.insertar(Configuracion(clave="AlarmaEstado2", valor="true"))

                                dao.insertar(Configuracion(clave="AlarmaHora3", valor="13"))
                                dao.insertar(Configuracion(clave="AlarmaMinutos3", valor="0"))
                                dao.insertar(Configuracion(clave="AlarmaEstado3", valor="true"))

                                dao.insertar(Configuracion(clave="AlarmaHora4", valor="17"))
                                dao.insertar(Configuracion(clave="AlarmaMinutos4", valor="0"))
                                dao.insertar(Configuracion(clave="AlarmaEstado4", valor="true"))

                                dao.insertar(Configuracion(clave="AlarmaHora5", valor="20"))
                                dao.insertar(Configuracion(clave="AlarmaMinutos5", valor="0"))
                                dao.insertar(Configuracion(clave="AlarmaEstado5", valor="true"))


                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
