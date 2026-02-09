package com.example.generadordeemparejamientos.domain.classes

import java.io.Serializable

/**
 * Class representing a round in a tournament.
 * @property numero The round number (starting from 1).
 * @property libre The name of the player who has a bye (if the number of players is odd), or null if there is no bye.
 * @property matches A mutable map that stores the results of the matches in this round.
 */
class Round(
    val numero: Int,
    val libre: Player?,
    val matches: Array<Match> = arrayOf()
) : Serializable {

    /**
     * A list of pairs of player names representing the matchups in this round. Each pair contains the names of the two players in a match. This property is derived from the matches array and provides a convenient way to access the matchups without needing to access the full Match objects.
     */
    val emparejamientos: List<Pair<String, String>>
        get() = matches.map { Pair(it.player1.name, it.player2.name) }

    /**
     * Retrieves the Match object for a given pair of player names.
     * @return returns the corresponding Match object; otherwise, it returns null.
     */
    fun getMatchByNames(player1Name: String, player2Name: String): Match? {
        return matches.find { (it.player1.name == player1Name && it.player2.name == player2Name) }
    }

    /**
     * Inserts the result of a match into the matches array. It searches for the match corresponding to the given player names and updates it with the provided result. If no matching match is found, the function does nothing.
     */
    fun insertResult(result: Match) {
        val matchIndex = matches.indexOfFirst { (it.player1.name == result.player1.name && it.player2.name == result.player2.name) || (it.player1.name == result.player2.name && it.player2.name == result.player1.name) }
        if (matchIndex != -1) {
            matches[matchIndex] = result
        }
    }
}