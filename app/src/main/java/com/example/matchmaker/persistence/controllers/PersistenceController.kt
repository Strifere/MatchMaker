package com.example.matchmaker.persistence.controllers

import android.content.Context
import android.net.Uri
import com.example.matchmaker.domain.classes.Tournament
import com.example.matchmaker.persistence.entities.TournamentEntity
import com.example.matchmaker.persistence.serialization.TournamentDeserializer
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

    fun exportTournament(
        context: Context,
        tournament: Tournament?,
        uri: Uri,
        onComplete: (Boolean, String?) -> Unit
    ) {
        if (tournament == null) {
            onComplete(false, "No hay torneo para exportar")
            return
        }

        val db = database
        if (db == null) {
            onComplete(false, "Base de datos no inicializada")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val nameCount = db.tournamentDAO.countByName(tournament.name)
                if (nameCount > 1) {
                    withContext(Dispatchers.Main) {
                        onComplete(false, "Hay torneos duplicados con el mismo nombre. Renombra antes de exportar.")
                    }
                    return@launch
                }

                val json = gson.toJson(tournament)
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(json.toByteArray())
                } ?: throw IllegalStateException("No se pudo abrir el archivo de exportación")

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

    suspend fun importTournament(context: Context, uri: Uri): Tournament? {
        val db = database ?: return null
        return withContext(Dispatchers.IO) {
            try {
                val json = context.contentResolver.openInputStream(uri)?.use { input ->
                    input.bufferedReader().readText()
                } ?: return@withContext null

                val tournament = gson.fromJson(json, Tournament::class.java) ?: return@withContext null

                var finalName = tournament.name
                var suffix = 1
                while (db.tournamentDAO.getTournamentByName(finalName) != null) {
                    finalName = "${tournament.name} ($suffix)"
                    suffix++
                }

                if (finalName != tournament.name) {
                    tournament.name = finalName
                }
                tournament.modifiedAt = System.currentTimeMillis()

                val entity = TournamentEntity(
                    name = tournament.name,
                    tournamentJson = gson.toJson(tournament)
                )
                db.tournamentDAO.insertTournament(entity)
                tournament
            } catch (_: Exception) {
                null
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

    suspend fun getTournamentByName(name: String): Tournament? {
        val db = database ?: return null
        return withContext(Dispatchers.IO) {
            try {
                val entity = db.tournamentDAO.getTournamentByName(name)
                if (entity != null) {
                        try {
                            gson.fromJson(entity.tournamentJson, Tournament::class.java)
                        } catch (_: Exception) {
                            null
                        }
                } else {
                    null
                }
            } catch (_: Exception) {
                null
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

    suspend fun updateTournamentName(currentTournament: Tournament?, newName: String): Boolean {
        if (currentTournament == null) return false
        val db = database ?: return false
        val oldName = currentTournament.name
        if (oldName == newName) return true

        return withContext(Dispatchers.IO) {
            try {
                if (db.tournamentDAO.countByName(newName) > 0) {
                    return@withContext false
                }

                val oldModifiedAt = currentTournament.modifiedAt
                currentTournament.name = newName
                currentTournament.modifiedAt = System.currentTimeMillis()
                val json = gson.toJson(currentTournament)

                val rows = db.tournamentDAO.renameTournament(oldName, newName, json)
                if (rows > 0) {
                    true
                } else {
                    currentTournament.name = oldName
                    currentTournament.modifiedAt = oldModifiedAt
                    false
                }
            } catch (_: Exception) {
                false
            }
        }
    }
}