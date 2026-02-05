package com.example.generadordeemparejamientos

import java.io.Serializable
import kotlin.math.max

class MatchResult (
    val player1Score: Int,
    val player2Score: Int,
    val includeSetResults: Boolean,
    val sets: LinkedHashMap<Int, SetResult> = linkedMapOf()
): Serializable {

    fun isMatchFinished(bestOf: Int): Boolean {
        // If both scores are 0, match hasn't started
        if (player1Score == 0 && player2Score == 0) {
            return false
        }

        // Calculate sets needed to win (more than half)
        val setsToWin = (bestOf / 2) + 1

        // Check if either player has won enough sets
        return player1Score >= setsToWin || player2Score >= setsToWin
    }

    // move to MatchResult
    fun shouldDisplayResult(): Boolean {
        // Don't display if both scores are 0
        return player1Score != 0 || player2Score != 0
    }

    // move to MatchResult
    // Checks that the match has not finished and, if it has, then that the results are correct
    fun checkMatchResult(bestOf: Int) : Boolean {
        var setsPlayed = 0
        var player1SetsWon = 0
        var player2SetsWon = 0
        for (i in 0 until sets.size) {
            val set = sets[i] ?: continue
            if (set.checkSetResult()) {
                setsPlayed++
                when (set.whoWonSet()) {
                    1 -> player1SetsWon++
                    2 -> player2SetsWon++
                }
                continue
            } else {
                return false
            }
        }
        // Check if the number of sets won matches the reported score
        if (includeSetResults && (player1SetsWon != player1Score || player2SetsWon != player2Score)) {
            return false
        }

        val maxLimit = (player1Score <= (bestOf / 2) + 1) && (player2Score <= (bestOf / 2) + 1)
        return if (max(player1Score,player2Score) == (bestOf / 2) + 1) {
            maxLimit && (player2Score !in player1Score..player1Score) // Checks that the losing player doesn't have more or equal sets won than the winning player
        } else maxLimit
    }
}