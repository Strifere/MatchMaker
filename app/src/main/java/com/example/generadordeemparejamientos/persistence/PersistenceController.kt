package com.example.generadordeemparejamientos.persistence

import android.app.Application

class PersistenceController : Application() {
    companion object {
        private var instance: PersistenceController? = null

        fun getInstance(): PersistenceController {
            if (instance == null) {
                instance = PersistenceController()
            }
            return instance!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}