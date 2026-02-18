package com.example.matchmaker.presentation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.matchmaker.R
import com.example.matchmaker.domain.classes.Tournament
import com.example.matchmaker.domain.controllers.DomainController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class LoadTournamentActivity : AppCompatActivity() {

    private lateinit var searchTournamentBar: SearchView
    private lateinit var tournamentsContainer: LinearLayout
    private lateinit var loadTournamentsTitle: TextView
    private val domainController = DomainController.getInstance()
    private var allTournaments: List<Tournament> = emptyList()
    private var pendingExportTournament: Tournament? = null

    private val exportTournamentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val tournament = pendingExportTournament
        if (uri == null || tournament == null) {
            pendingExportTournament = null
            return@registerForActivityResult
        }

        domainController.exportTournament(this, tournament, uri) { success, error ->
            if (success) {
                Toast.makeText(this, "Torneo exportado correctamente", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Error al exportar el torneo: $error", Toast.LENGTH_LONG).show()
            }
            pendingExportTournament = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_tournament)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        searchTournamentBar = findViewById(R.id.searchButton)
        tournamentsContainer = findViewById(R.id.tournamentsContainer)
        loadTournamentsTitle = findViewById(R.id.loadTournamentTitle)

        backButton.setOnClickListener { finish() }

        searchTournamentBar.setOnClickListener { loadTournamentsTitle.visibility = View.GONE }
        searchTournamentBar.setOnCloseListener {
            loadTournamentsTitle.visibility = View.VISIBLE
            false
        }
        searchTournamentBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterTournaments(newText ?: "")
                return true
            }
        })

        renderTournaments()
    }

    private fun renderTournaments() {
        lifecycleScope.launch {
            try {
                // Fetch all tournaments from database
                allTournaments = domainController.getAllTournaments()

                // Clear existing views
                tournamentsContainer.removeAllViews()

                if (allTournaments.isEmpty()) {
                    // Show message when no tournaments are saved
                    val emptyView = TextView(this@LoadTournamentActivity).apply {
                        text = "No hay torneos guardados"
                        textSize = 18f
                        setPadding(16, 32, 16, 16)
                        textAlignment = View.TEXT_ALIGNMENT_CENTER
                    }
                    tournamentsContainer.addView(emptyView)
                } else {
                    // Create a card for each tournament
                    allTournaments.forEach { tournament ->
                        createTournamentCard(tournament)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@LoadTournamentActivity,
                    "Error al cargar torneos: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun createTournamentCard(tournament: Tournament) {
        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.tournament_card, tournamentsContainer, false)

        val tournamentNameView = cardView.findViewById<TextView>(R.id.tournamentName)
        val tournamentDateText = cardView.findViewById<TextView>(R.id.dateText)
        val modifiedDateText = cardView.findViewById<TextView>(R.id.modifiedText)
        val tournamentPlayersText = cardView.findViewById<TextView>(R.id.playersText)
        val bestoOfText = cardView.findViewById<TextView>(R.id.bestOfText)
        val includeSetsText = cardView.findViewById<TextView>(R.id.setsIncludedText)
        val loadButton = cardView.findViewById<Button>(R.id.loadButton)
        val deleteButton = cardView.findViewById<Button>(R.id.deleteButton)
        val shareButton = cardView.findViewById<ImageButton>(R.id.shareButton)

        tournamentNameView.text = tournament.name
        tournamentDateText.text = "Creado el: ${SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault()).format(tournament.createdAt)}"
        modifiedDateText.text = "Última modificación: ${SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault()).format(tournament.modifiedAt)}"

        tournamentPlayersText.text = "Jugadores: ${tournament.players.size}"
        bestoOfText.text = "Mejor de: ${tournament.bestOf} sets"
        includeSetsText.text = if (tournament.includeSetResults) "Incluye resultados de sets" else "No incluye resultados de sets"

        loadButton.setOnClickListener {
            loadTournament(tournament)
        }
        deleteButton.setOnClickListener {
            deleteTournament(tournament)
        }
        shareButton.setOnClickListener {
            pendingExportTournament = tournament
            exportTournamentLauncher.launch("${tournament.name}.json")
        }

        tournamentsContainer.addView(cardView)
    }

    private fun loadTournament(tournament: Tournament) {
        // Set the loaded tournament as the current tournament
        domainController.setTournament(tournament)

        // Navigate to TournamentActivity
        val intent = Intent(this, TournamentActivity::class.java)
        startActivity(intent)

        // Finish this activity so back button goes to MainActivity
        finish()
    }

    private fun deleteTournament(tournament: Tournament) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar torneo ${tournament.name}")
            .setView(TextView(this).apply {
                text = "¿Estás seguro de que quieres eliminar este torneo?."
                setPadding(50, 40, 50, 40)
            })
            .setPositiveButton("Sí") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val success = domainController.deleteTournament(tournament.name)
                        if (success) {
                            Toast.makeText(
                                this@LoadTournamentActivity,
                                "Torneo eliminado",
                                Toast.LENGTH_SHORT
                            ).show()
                            renderTournaments()
                        } // Refresh the list after deletion
                        else {
                            Toast.makeText(
                                this@LoadTournamentActivity,
                                "Error al eliminar el torneo",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@LoadTournamentActivity,
                            "Error al eliminar el torneo: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun filterTournaments(query: String) {
        for (i in 0 until tournamentsContainer.childCount) {
            val tournamentView = tournamentsContainer.getChildAt(i)
            val nameTextView = tournamentView.findViewById<TextView>(R.id.tournamentName)
            if (nameTextView != null) {
                val tournamentName = nameTextView.text.toString()
                tournamentView.visibility = if (tournamentName.contains(query, ignoreCase = true)) View.VISIBLE else View.GONE
            }
        }
    }
}