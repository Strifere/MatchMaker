package com.example.generadordeemparejamientos

import android.app.Application

class TournamentApplication : Application() {
    companion object {
        private var currentTournament: Tournament? = null

        fun setTournament(tournament: Tournament) {
            currentTournament = tournament
        }

        fun getTournament(): Tournament? {
            return currentTournament
        }

        fun clearTournament() {
            currentTournament = null
        }
    }
}
