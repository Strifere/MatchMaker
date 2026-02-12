package com.example.generadordeemparejamientos.persistence.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Set(
    @PrimaryKey(autoGenerate = true) val id: Int,
    var player1Points: Int,
    var player2Points: Int,
)
