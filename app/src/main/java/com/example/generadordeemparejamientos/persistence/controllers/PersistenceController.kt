package com.example.generadordeemparejamientos.persistence.controllers

import android.content.Context
import com.example.generadordeemparejamientos.domain.classes.Tournament
import com.example.generadordeemparejamientos.persistence.entities.TournamentEntity
import com.example.generadordeemparejamientos.persistence.serialization.TournamentDeserializer
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PersistenceController private constructor() {
    companion object {

        @Volatile
        private var instance: PersistenceController? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: PersistenceController().also { instance = it }
            }
    }

    private var database: TournamentDatabase? = null
    private val gson = GsonBuilder()
        .registerTypeAdapter(Tournament::class.java, TournamentDeserializer())
        .create()

    fun initialize(context: Context) {
        if (database == null) {
            database = TournamentDatabase.getInstance(context)
        }
    }

    fun saveTournament(tournament: Tournament?, onComplete: (Boolean, String?) -> Unit) {
        if (tournament == null) {
            onComplete(false, "No hay torneo para guardar")
            return
        }

        val db = database
        if (db == null) {
            onComplete(false, "Base de datos no inicializada")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tournamentJson = gson.toJson(tournament)
                val entity = TournamentEntity(
                    name = tournament.name,
                    tournamentJson = tournamentJson
                )
                db.tournamentDAO.insertTournament(entity)
                withContext(Dispatchers.Main) {
                    onComplete(true, null)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false, e.message)
                }
            }
        }
    }

    suspend fun loadTournament(name: String): Tournament? {
        val db = database ?: return null
        return withContext(Dispatchers.IO) {
            try {
                val entity = db.tournamentDAO.getTournamentByName(name)
                entity?.let {
                    gson.fromJson(it.tournamentJson, Tournament::class.java)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getAllTournaments(): List<Tournament> {
        val db = database ?: return emptyList()
        return withContext(Dispatchers.IO) {
            try {
                val entities = db.tournamentDAO.getAllTournaments()
                entities.mapNotNull { entity ->
                    try {
                        gson.fromJson(entity.tournamentJson, Tournament::class.java)
                    } catch (_: Exception) {
                        null
                    }
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getTournamentByName(name: String): List<Tournament> {
        val db = database ?: return emptyList()
        return withContext(Dispatchers.IO) {
            try {
                val entity = db.tournamentDAO.getTournamentByName(name)
                if (entity != null) {
                    listOfNotNull(
                        try {
                            gson.fromJson(entity.tournamentJson, Tournament::class.java)
                        } catch (_: Exception) {
                            null
                        }
                    )
                } else {
                    emptyList()
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
    suspend fun deleteTournament(name: String): Boolean {
        val db = database ?: return false
        return withContext(Dispatchers.IO) {
            try {
                val entity = db.tournamentDAO.getTournamentByName(name)
                if (entity != null) {
                    db.tournamentDAO.deleteTournament(entity)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }
}