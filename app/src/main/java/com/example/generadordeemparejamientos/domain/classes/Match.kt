package com.example.generadordeemparejamientos.domain.classes

import java.io.Serializable
import kotlin.math.max

/**
 * Class representing the result of a match between two players.
 * @property player1Sets The number of sets won by player 1.
 * @property player2Sets The number of sets won by player 2.
 * @property includeSetResults A boolean indicating whether the detailed set results are included in the match result.
 * @property sets A LinkedHashMap that stores the results of each set in the match, where the key is the set number (starting from 0) and the value is a SetResult object representing the result of that set.
 * The class provides methods to check if the match has finished based on the scores and the best-of format, to determine if the match result should be displayed, and to validate the match result against the included set results and the best-of format.
 */
class Match (
    var player1 : Player,
    var player2 : Player,
    val player1Sets: Int,
    val player2Sets: Int,
    val includeSetResults: Boolean,
    val sets: LinkedHashMap<Int, Set> = linkedMapOf()
): Serializable {

    /**
     * Checks if the match has finished.
     * @param bestOf Maximum sets per match (the set is won if one of the players wins (bestOf / 2) + 1 sets).
     * @return true if the match has finished, false otherwise.
     */
    fun isMatchFinished(bestOf: Int): Boolean {
        // If both scores are 0, match hasn't started
        if (player1Sets == 0 && player2Sets == 0) {
            return false
        }

        // Calculate sets needed to win (more than half)
        val setsToWin = (bestOf / 2) + 1

        // Check if either player has won enough sets
        return player1Sets >= setsToWin || player2Sets >= setsToWin
    }

    /**
     * Helper function to determine if the result should be displayed in presentation layer.
     * @return true if the match has started (at least one player has won a set), false otherwise. This is avoids displaying a 0-0 score for matches that haven't started yet.
     */
    fun shouldDisplayResult(): Boolean {
        // Don't display if both scores are 0
        return player1Sets != 0 || player2Sets != 0
    }


    /**
     * Checks that the match has not finished and, if it has, then that the results are correct.
     * @param bestOf Maximum sets per match (the set is won if one of the players wins (bestOf / 2) + 1 sets).
     * @return true if the match result is valid, false if:
     * - The match has finished but the number of sets won by each player does not match the included set results (if includeSetResults is true).
     * - The match has finished but the number of sets won by either player exceeds the maximum possible based on the best-of format.
     * - The match has finished but the losing player has won more or equal sets than the winning player, which would indicate an inconsistency in the reported scores.
     */
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
        if (includeSetResults && (player1SetsWon != player1Sets || player2SetsWon != player2Sets)) {
            return false
        }

        val maxLimit = (player1Sets <= (bestOf / 2) + 1) && (player2Sets <= (bestOf / 2) + 1)
        return if (max(player1Sets,player2Sets) == (bestOf / 2) + 1) {
            maxLimit && (player2Sets !in player1Sets..player1Sets) // Checks that the losing player doesn't have more or equal sets won than the winning player
        } else maxLimit
    }
}