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

    fun generateMatchResult(player1Score : Int?, player2Score : Int?, setResult: LinkedHashMap<Int, SetResult>): MatchResult {
        // If the includeSetResults flag is true, it computes the overall result and checks that is correct
        if (includeSetResults) {
            var player1Sets = 0
            var player2Sets = 0
            for (i in 0 until setResult.size) {
                val set = setResult[i] ?: continue
                when (set.whoWonSet()) {
                    1 -> player1Sets++
                    2 -> player2Sets++
                    0 -> { /* Set not finished, do nothing */}
                }
            }
            return MatchResult(
                player1Score = player1Sets,
                player2Score = player2Sets,
                includeSetResults = true,
                sets = setResult
            )
        }
        // If the includeSetResults flag is false, it just creates the MatchResult object
        else {
            return MatchResult(
                player1Score = player1Score ?: 0,
                player2Score = player2Score ?: 0,
                includeSetResults = false,
                sets = setResult
            )
        }
    }
}