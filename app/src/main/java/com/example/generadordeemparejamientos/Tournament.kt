package com.example.generadordeemparejamientos

import java.io.Serializable

class Tournament (
    var rondas: MutableList<Ronda> = mutableListOf(),
    val numJugadores: Int,
    val nombres: Array<String>,
    val tabla: Array<IntArray>,
    val bestOf: Int,
    val includeSetResults : Boolean
) : Serializable {
    init {
        val nRondas = if (numJugadores % 2 == 1) numJugadores else numJugadores - 1
        for (ronda in 0 until nRondas) {
            val usados = MutableList(numJugadores) { false }
            val emparejamientos = mutableListOf<Pair<String, String>>()
            for (i in 0 until numJugadores) {
                for (j in i + 1 until numJugadores) {
                    if (tabla[i][j] == ronda) {
                        usados[i] = true
                        usados[j] = true
                        emparejamientos.add(nombres[i] to nombres[j])
                    }
                }
            }

            var libreIndex = -1
            for (i in 0 until numJugadores) {
                if (!usados[i]) {
                    libreIndex = i
                    break
                }
            }
            val libre = if (libreIndex != -1) nombres[libreIndex] else null
            rondas.add(Ronda(ronda + 1, emparejamientos, libre))
        }
    }
    fun isMatchFinished(result: MatchResult): Boolean {
        // If both scores are 0, match hasn't started
        if (result.player1Score == 0 && result.player2Score == 0) {
            return false
        }

        // Calculate sets needed to win (more than half)
        val setsToWin = (bestOf / 2) + 1

        // Check if either player has won enough sets
        return result.player1Score >= setsToWin || result.player2Score >= setsToWin
    }

    fun shouldDisplayResult(result: MatchResult): Boolean {
        // Don't display if both scores are 0
        return result.player1Score != 0 || result.player2Score != 0
    }
}