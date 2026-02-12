package com.example.generadordeemparejamientos.persistence.controllers

import com.example.generadordeemparejamientos.domain.classes.Tournament

class PersistenceController private constructor() {
    companion object {

        @Volatile
        private var instance: PersistenceController? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: PersistenceController().also { instance = it }
            }
    }

    fun saveTournament(tournament: Tournament?) {

    }
}