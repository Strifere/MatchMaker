package com.example.generadordeemparejamientos

import java.io.Serializable
import kotlin.math.abs
import kotlin.math.min

class SetResult (
    val player1Points: Int,
    val player2Points: Int
) : Serializable {

    // Checks that the set has not finished and, if it has, then that the results are correct
    fun checkSetResult(): Boolean {
        val player1Points = player1Points
        val player2Points = player2Points
        return if (min(player1Points, player2Points) >= 10) {
            (abs(player1Points - player2Points) <= 2)
        } else {
            (maxOf(player1Points, player2Points) <= 11)
        }
    }

    fun whoWonSet(): Int {
        return if (isSetFinished()) {
            if (player1Points > player2Points) 1 else 2
        } else 0
    }

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