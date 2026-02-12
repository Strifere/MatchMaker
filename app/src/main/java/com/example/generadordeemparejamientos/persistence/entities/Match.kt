package com.example.generadordeemparejamientos.persistence.entities

import androidx.room.Entity

@Entity(primaryKeys = ["player1", "player2"])
data class Match(
    val player1: Player,
    val player2: Player,
    var player1Sets: Int = 0,
    var player2Sets: Int = 0,
    val includeSetResults: Boolean,
    val sets: LinkedHashMap<Int, Set> = linkedMapOf()
)
