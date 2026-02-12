package com.example.generadordeemparejamientos.persistence.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Round(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val numero: Int,
    val libre: Player?,
    val matches: List<Match>
)