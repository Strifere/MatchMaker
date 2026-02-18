package com.example.matchmaker.domain.classes

import java.io.Serializable
import kotlin.math.abs
import kotlin.math.min

/**
 * Class representing the result of a set in a match.
 * @property player1Points Points scored by player 1 in the set.
 * @property player2Points Points scored by player 2 in the set.
 * The rules for winning a set are as follows:
 * - A player wins the set if they reach 11 points and are at least 2 points ahead of the opponent.
 * - If both players reach 10 points, the set continues until one player is 2 points ahead of the other.
 * Any result that does not follow these rules is considered invalid. The class provides methods to check the validity of the set result, determine if the set has finished, and identify the winner of the set.
 * The checks won't return false if the set is not finished because it's possible that the user wants to input the points of a set that is still ongoing.
 */
class Set (
    val player1Points: Int,
    val player2Points: Int
) : Serializable {

    /**
     * Checks if the set result is valid according to the rules of the game. A valid set result must satisfy the following conditions:
     * - Both players' points must be non-negative.
     * - If both players have at least 10 points, the difference between their points must be at most 2.
     * - If at least one player has less than 10 points, the maximum points for either player must be at most 11.
     * @return true if the set result is valid, false otherwise.
     */
    fun checkSetResult(): Boolean {
        val player1Points = player1Points
        val player2Points = player2Points
        return if (min(player1Points, player2Points) >= 10) {
            (abs(player1Points - player2Points) <= 2)
        } else {
            (maxOf(player1Points, player2Points) <= 11)
        }
    }

    /**
     * Determines the winner of the set.
     * @return 1 if player 1 won the set, 2 if player 2 won the set, or 0 if the set has not finished yet.
     */
    fun whoWonSet(): Int {
        return if (isSetFinished()) {
            if (player1Points > player2Points) 1 else 2
        } else 0
    }

    /**
     * Checks if the set has finished according to the rules of the game. A set is considered finished if:
     * - If both players have at least 10 points, the difference between their points is exactly 2.
     * - If at least one player has less than 10 points, the maximum points for either player is exactly 11.
     * @return true if the set has finished, false otherwise.
     */
    fun isSetFinished(): Boolean {
        val player1Points = player1Points
        val player2Points = player2Points
        return if (min(player1Points, player2Points) >= 10) {
            abs(player1Points - player2Points) == 2
        } else {
            maxOf(player1Points, player2Points) == 11
        }
    }

}