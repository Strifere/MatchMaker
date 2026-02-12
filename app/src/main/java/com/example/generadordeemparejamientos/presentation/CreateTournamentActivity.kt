package com.example.generadordeemparejamientos.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.generadordeemparejamientos.R
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
            val numJugadores = input.text.toString().trim().toIntOrNull()
            val numSets = sets.text.toString().trim().toIntOrNull()
            val includeSets = includeSetsResults.isChecked
            var tournamentName = tournamentNameEdit.text.toString().trim()
            if (tournamentName == "") tournamentName = "Nuevo Torneo ${LocalDateTime.now()}"
            if (numJugadores == null || numJugadores < 2) {
                resultText.text = "El número de jugadores debe ser al menos 2."
                return@setOnClickListener
            }
            if (numSets == null) {
                resultText.text = "Introduzca el número de sets máximo por partido."
                return@setOnClickListener
            } else if (numSets%2 == 0) {
                resultText.text = "El número de sets debe ser impar."
                return@setOnClickListener
            }

            // Navigate to NamesActivity
            val intent = Intent(this, NamesActivity::class.java)
            intent.putExtra("numJugadores", numJugadores)
            intent.putExtra("numSets", numSets)
            intent.putExtra("includeSetsResults", includeSetsResults.isChecked)
            intent.putExtra("includeSetsResults", includeSets)
            intent.putExtra("tournamentName", tournamentName)
            startActivity(intent)
        }
    }
}