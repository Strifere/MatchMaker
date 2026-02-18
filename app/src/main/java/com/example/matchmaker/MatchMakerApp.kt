package com.example.matchmaker

import android.app.Application
import com.example.matchmaker.domain.controllers.DomainController

class MatchMakerApp : Application() {
    override fun onTerminate() {
        super.onTerminate()
        // Code executed when app terminates
        DomainController.getInstance().saveTournament(this, true)
    }
}
