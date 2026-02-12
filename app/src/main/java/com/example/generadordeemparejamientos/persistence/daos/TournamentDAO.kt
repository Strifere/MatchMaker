package com.example.generadordeemparejamientos.persistence.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.generadordeemparejamientos.persistence.entities.TournamentEntity

@Dao
interface TournamentDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: TournamentEntity)

    @Delete
    suspend fun deleteTournament(tournament: TournamentEntity)

    @Update
    suspend fun updateTournament(tournament: TournamentEntity)

    @Query("SELECT * FROM tournament ORDER BY name ASC")
    suspend fun getAllTournaments(): List<TournamentEntity>

    @Query("SELECT * FROM tournament WHERE name = :tournamentName")
    suspend fun getTournamentByName(tournamentName: String): TournamentEntity?

    @Query("SELECT * FROM tournament WHERE name LIKE '%' || :name || '%' ORDER BY name ASC")
    suspend fun searchTournamentByName(name: String): List<TournamentEntity>
}