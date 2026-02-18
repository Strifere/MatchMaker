package com.example.matchmaker.persistence.controllers

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.matchmaker.persistence.daos.TournamentDAO
import com.example.matchmaker.persistence.entities.TournamentEntity

@Database(
    entities = [TournamentEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TournamentDatabase : RoomDatabase() {
    abstract val tournamentDAO: TournamentDAO

    companion object {
        @Volatile
        private var INSTANCE: TournamentDatabase? = null

        fun getInstance(context: Context): TournamentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TournamentDatabase::class.java,
                    "tournament_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}