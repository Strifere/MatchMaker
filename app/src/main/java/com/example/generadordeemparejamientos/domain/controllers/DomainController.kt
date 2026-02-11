package com.example.generadordeemparejamientos.domain.controllers

import android.app.Application
import com.example.generadordeemparejamientos.domain.classes.Player
import com.example.generadordeemparejamientos.domain.classes.Tournament

/**
 * DomainController is a singleton class that extends Application and is used to store the current tournament data.
 * It provides methods to set, get, and clear the current tournament.
 */
class DomainController : Application() {
    companion object {
        private var currentTournament: Tournament? = null

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
         * Sets up the [Tournament] object with the provided parameters and generates the matchups table.
         * @param numJugadores The number of players in the tournament.
         * @param nombres An array of player names corresponding to the number of players.
         * @param numSets The number of sets to be played in each match.
         * @param includeSetsResults A boolean indicating whether to include the results of individual sets in the tournament data.
         * @return A [Tournament] object initialized with the provided parameters and the generated matchups table.
         */
        fun createTournament(name: String, creator: String, numJugadores: Int, nombres: Array<String>, numSets: Int, includeSetsResults: Boolean): Tournament {
            val tabla = generarTablaEmparejamientos(numJugadores)
            val players = createPlayersList(numJugadores, nombres)
            val tournament = Tournament(
                name = name,
                creator = creator,
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
}