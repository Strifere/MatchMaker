package com.example.generadordeemparejamientos

import java.io.Serializable
import kotlin.math.abs
import kotlin.math.min

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

    // Checks that the match has not finished and, if it has, then that the results are correct
    fun checkMatchResult(result : MatchResult) : Boolean {
        var setsPlayed = 0
        var player1SetsWon = 0
        var player2SetsWon = 0
        for (i in 0 until result.sets.size) {
            val set = result.sets[i] ?: continue
            if (checkSetResult(set)) {
                setsPlayed++
                when (whoWonSet(set)) {
                    1 -> player1SetsWon++
                    2 -> player2SetsWon++
                }
                continue
            } else {
                return false
            }
        }
        // Check if the number of sets won matches the reported score
        if (player1SetsWon != result.player1Score || player2SetsWon != result.player2Score) {
            return false
        }
        return (result.player1Score <= (bestOf / 2) + 1) && (result.player2Score <= (bestOf / 2) + 1)

    }

    // Checks that the set has not finished and, if it has, then that the results are correct
    fun checkSetResult(set: SetResult): Boolean {
        val player1Points = set.player1Points
        val player2Points = set.player2Points
        return if (min(player1Points, player2Points) >= 10) {
            (abs(player1Points - player2Points) <= 2)
        } else {
            (maxOf(player1Points, player2Points) <= 11)
        }
    }

    fun whoWonSet(result: SetResult): Int {
        return if (isSetFinished(result)) {
            if (result.player1Points > result.player2Points) 1 else 2
        } else 0
    }

    fun isSetFinished(result: SetResult): Boolean {
        val player1Points = result.player1Points
        val player2Points = result.player2Points
        return if (min(player1Points, player2Points) >= 10) {
            abs(player1Points - player2Points) == 2
        } else {
            maxOf(player1Points, player2Points) == 11
        }
    }
}