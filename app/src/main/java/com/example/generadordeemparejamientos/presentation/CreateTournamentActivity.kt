package com.example.generadordeemparejamientos.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.generadordeemparejamientos.R
import com.example.generadordeemparejamientos.domain.controllers.DomainController
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class CreateTournamentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_tournament)

        val input = findViewById<EditText>(R.id.inputBox)
        val sets = findViewById<EditText>(R.id.setsBox)
        val submitButton = findViewById<Button>(R.id.submitButton)
        val resultText = findViewById<TextView>(R.id.testo)
        val includeSetsResults = findViewById<CheckBox>(R.id.includeSetsResults)
        val tournamentNameEdit = findViewById<EditText>(R.id.tournamentTitle)
        val backButton = findViewById<ImageButton>(R.id.backButton)

        backButton.setOnClickListener { finish() }

        submitButton.setOnClickListener {
            lifecycleScope.launch {
                val numJugadores = input.text.toString().trim().toIntOrNull()
                val numSets = sets.text.toString().trim().toIntOrNull()
                val includeSets = includeSetsResults.isChecked
                var tournamentName = tournamentNameEdit.text.toString().trim()
                if (tournamentName.isBlank()) tournamentName = "Nuevo Torneo ${LocalDateTime.now()}"

                val tournamentExists = DomainController.getInstance().getTournamentsByName(tournamentName) != null
                if (tournamentExists) {
                    resultText.text = "Ya existe un torneo con ese nombre. Por favor, elija otro."
                    return@launch
                }
                if (numJugadores == null || numJugadores < 2) {
                    resultText.text = "El número de jugadores debe ser al menos 2."
                    return@launch
                }
                if (numSets == null) {
                    resultText.text = "Introduzca el número de sets máximo por partido."
                    return@launch
                } else if (numSets%2 == 0) {
                    resultText.text = "El número de sets debe ser impar."
                    return@launch
                }

                // Navigate to NamesActivity
                namesActivity(numJugadores, numSets, includeSets, tournamentName)
            }
        }
    }

    private fun namesActivity(numJugadores: Int, numSets: Int, includeSets: Boolean, tournamentName: String) {
        val intent = Intent(this, NamesActivity::class.java)
        intent.putExtra("numJugadores", numJugadores)
        intent.putExtra("numSets", numSets)
        intent.putExtra("includeSetsResults", includeSets)
        intent.putExtra("tournamentName", tournamentName)
        startActivity(intent)
    }
}