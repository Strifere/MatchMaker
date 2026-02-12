package com.example.generadordeemparejamientos.persistence.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tournament")
data class TournamentEntity(
    @PrimaryKey var name: String,
    val tournamentJson: String
)
