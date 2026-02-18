package com.example.matchmaker.domain.controllers

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.example.matchmaker.domain.classes.Player
import com.example.matchmaker.domain.classes.Tournament
import com.example.matchmaker.persistence.controllers.PersistenceController

/**
 * DomainController is a singleton class that is used to manage the current tournament data.
 * It provides methods to set, get, and clear the current tournament, and so communication with the persistence layer.
 */
class DomainController private constructor() {
    companion object {
        @Volatile
        private var instance : DomainController? = null
        private var currentTournament: Tournament? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: DomainController().also { instance = it }
            }
    }
    private val persistenceController: PersistenceController
        get() = PersistenceController.getInstance()

    /**
     * Initializes the [DomainController] and also the [PersistenceController]
     */
    fun initialize(context: Context) {
        persistenceController.initialize(context)
    }

    /**
     * Sets the current tournament.
     * @param tournament The [Tournament] object to be stored as the current tournament.
     */
    fun setTournament(tournament: Tournament) {
        currentTournament = tournament
    }

    /**
     * Retrieves the current tournament.
     * @return The current [Tournament] object, or null if no tournament is set.
     */
    fun getTournament(): Tournament? {
        return currentTournament
    }

    /**
     * Calls the [PersistenceController] to save the tournament data in the database.
     * @param context the context in which the result of the operation will be displayed
     */
    fun saveTournament(context: Context, displayMessage: Boolean) {
        try {
            persistenceController.saveTournament(currentTournament) { success, error ->
                if (success) {
                    if (displayMessage) Toast.makeText(context, "Torneo guardado correctamente", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Error al guardar el torneo: $error", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al guardar el torneo: ${e.message}", Toast.LENGTH_LONG).show()
        }

    }

    /**
     * Calls the [persistenceController] to export a given tournament in a given location.
     * @param context the context from where the export is being done.
     * @param tournament the [Tournament] object that will be exported.
     * @param uri the URI where [tournament] will be exported.
     * @param onComplete acts as a mechanism to know if the transaction was successful or not and the reason why.
     */
    fun exportTournament(context: Context, tournament: Tournament, uri: Uri, onComplete: (Boolean, String?) -> Unit) {
        persistenceController.exportTournament(context, tournament, uri, onComplete)
    }

    /**
     * Calls the [persistenceController] to import a tournament from a given location.
     * @param context the context from where the import is being done.
     * @param uri the URI where the [Tournament] object is being imported.
     * @return the [Tournament] object that has been imported or null if something went wrong.
     */
    suspend fun importTournament(context: Context, uri: Uri): Tournament? {
        return persistenceController.importTournament(context, uri)
    }

    /**
     * Calls the [persistenceController] to update the name of the [currentTournament] in the database.
     * @param newName the new name that the [currentTournament] will have.
     * @return
     *  - true: the name of the [currentTournament] was updated in the database.
     *  - false: the name of the [currentTournament] was not updated in the database (it doesn't exist or the name is already in use).
     */
    suspend fun updateTournamentName(newName: String): Boolean {
        return persistenceController.updateTournamentName(currentTournament, newName)
    }

    /**
     * Calls the [persistenceController] to load a tournament that is identified by the given name
     * @param name the identifier of the [Tournament] object that is being loaded.
     * @return a [Tournament] object identified by [name] or null if no tournament in the database is identified by that [name].
     */
    suspend fun loadTournament(name: String): Tournament? {
        return persistenceController.loadTournament(name)
    }

    /**
     * Calls the [persistenceController] to get all the tournaments in the database.
     * @return a [List] of [Tournament] objects that have been loaded from the database.
     */
    suspend fun getAllTournaments(): List<Tournament> {
        return persistenceController.getAllTournaments()
    }

    /**
     * Calls the [persistenceController] to get the tournament in the database that has the given name.
     * @param name the identifier of the [Tournament] object that will be returned.
     * @return a [Tournament] object identified by [name] or null if there is no tournament in the database with that [name].
     */
    suspend fun getTournamentsByName(name: String): Tournament? {
        return persistenceController.getTournamentByName(name)
    }

    /**
     * Calls the [persistenceController] to delete the tournament in the database that is identified by the given name.
     * @param name the name of the tournament that is being targeted for deletion
     * @return
     *  - true: if there was a tournament in the database that was identified by [name] was deleted
     *  - false: if there was no tournament in the database that is identified by [name]
     */
    suspend fun deleteTournament(name: String): Boolean {
        return persistenceController.deleteTournament(name)
    }

    /**
     * Sets up the [Tournament] object with the provided parameters and generates the matchups table.
     * @param numJugadores The number of players in the tournament.
     * @param nombres An array of player names corresponding to the number of players.
     * @param numSets The number of sets to be played in each match.
     * @param includeSetsResults A boolean indicating whether to include the results of individual sets in the tournament data.
     * @return A [Tournament] object initialized with the provided parameters and the generated matchups table.
     */
    fun createTournament(name: String, numJugadores: Int, nombres: Array<String>, numSets: Int, includeSetsResults: Boolean): Tournament {
        val tabla = generarTablaEmparejamientos(numJugadores)
        val players = createPlayersList(numJugadores, nombres)
        val tournament = Tournament(
            name = name,
            createdAt = System.currentTimeMillis(),
            players = players,
            bestOf = numSets,
            includeSetResults = includeSetsResults
        )
        tournament.initialize(tabla)
        return tournament
    }

    /**
     * Shifts the elements of the [cruces] list to the right. The first element is moved to the end of the list.
     * @param cruces The list of integers representing the current order of players for generating matchups.
     * @param n The size of the [cruces] list.
     */
    private fun shiftNumbers(cruces: MutableList<Int>, n: Int) {
        var aux = cruces[n-1]
        for (i in 1 until n) {
            val temp = cruces[i]
            cruces[i] = aux
            aux = temp
        }
    }

    /**
     * Generates a table of matchups for a round-robin tournament based on the number of players. If the number of players is odd, a "bye" player is added to ensure that each player has an opponent in each round.
     * @param numJugadores The number of players in the tournament.
     * @return A 2D list representing the matchup table, where the value at row i column j indicates the round in which player i and player j will face each other. A value of -1 indicates that the players do not face each other, and a value of -2 indicates the diagonal of the table (where i == j). If the number of players is odd, the table will include an additional "bye" player, and the matchups will be adjusted accordingly.
     */
    private fun generarTablaEmparejamientos(numJugadores: Int): List<List<Int>> {
        val impar = numJugadores % 2 == 1
        val n = if (impar) numJugadores + 1 else numJugadores
        val nRondas = n - 1
        val maxEmparejamientosPorRonda = n / 2
        var ronda = 0
        val tablaTemporal = MutableList(n) { fila ->
            MutableList(n) { columna -> if (fila >= columna) -2 else -1 }
        }
        val cruces = (0 until n).toMutableList()
        while (ronda < nRondas) {
            var contador = 0
            while (contador < maxEmparejamientosPorRonda) {
                val jugador1 = cruces[contador]
                val jugador2 = cruces[n - 1 - contador]
                if (jugador1 < jugador2) tablaTemporal[jugador1][jugador2] = ronda
                else if (jugador1 > jugador2) tablaTemporal[jugador2][jugador1] = ronda
                contador++
            }
            shiftNumbers(cruces, n)
            ++ronda
        }

        if (impar) {
            // Remove the "bye" player from the table
            val tablaSinBye = MutableList(numJugadores) {
                MutableList(numJugadores) { -2 }
            }
            for (i in 0 until numJugadores) {
                for (j in 0 until numJugadores) {
                    tablaSinBye[i][j] = tablaTemporal[i][j]
                }
            }
            return tablaSinBye
        }
        else return tablaTemporal
    }

    /**
     * Creates a list of [Player] objects based on the provided number of players and their corresponding names.
     * @param numJugadores The number of players in the tournament.
     * @param nombres An array of player names corresponding to the number of players.
     * @return An array of [Player] objects initialized with the provided names.
     */
    private fun createPlayersList(numJugadores: Int, nombres: Array<String>): Array<Player> {
        val players = Array(numJugadores) { Player(name = "") }
        for (i in 0 until numJugadores) {
            val name = nombres[i]
            val player = Player(name = name)
            players[i] = player
        }
        return players
    }
}
