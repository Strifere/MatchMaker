package com.example.generadordeemparejamientos.domain.controllers

import android.app.Application
import com.example.generadordeemparejamientos.domain.classes.Tournament

/**
 * DomainController is a singleton class that extends Application and is used to store the current tournament data.
 * It provides methods to set, get, and clear the current tournament.
 */
class DomainController : Application() {
    companion object {
        private var currentTournament: Tournament? = null

        /**
         * Sets the current tournament.
         * @param tournament The Tournament object to be stored as the current tournament.
         */
        fun setTournament(tournament: Tournament) {
            currentTournament = tournament
        }

        /**
         * Retrieves the current tournament.
         * @return The current Tournament object, or null if no tournament is set.
         */
        fun getTournament(): Tournament? {
            return currentTournament
        }
    }
}