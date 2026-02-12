package com.example.generadordeemparejamientos.persistence.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Player(
    @PrimaryKey var name: String,
    var pj: Int = 0,
    var pg: Int = 0,
    var pp: Int = 0,
    var sf: Int = 0,
    var sc: Int = 0,
    var pts: Int = 0
)
