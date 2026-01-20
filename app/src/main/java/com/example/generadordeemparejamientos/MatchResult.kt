package com.example.generadordeemparejamientos

import java.io.Serializable

class MatchResult (
    val player1Score: Int,
    val player2Score: Int,
    val sets: LinkedHashMap<Int, SetResult> = linkedMapOf()
): Serializable {

}