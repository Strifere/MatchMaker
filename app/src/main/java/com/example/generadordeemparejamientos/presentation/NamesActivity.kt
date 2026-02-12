package com.example.generadordeemparejamientos.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.example.generadordeemparejamientos.R
import com.example.generadordeemparejamientos.domain.controllers.DomainController

class NamesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_names)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val generateButton = findViewById<Button>(R.id.generateButton)
        val generateEmptyButton = findViewById<Button>(R.id.generateNoNamesButton)
        val namesContainer = findViewById<LinearLayout>(R.id.namesContainer)

        val tournamentName = intent.getStringExtra("tournamentName")!!
        val numJugadores = intent.getIntExtra("numJugadores", 0)
        val numSets = intent.getIntExtra("numSets", 1)
        val includeSetsResults = intent.getBooleanExtra("includeSetsResults", false)

        // Create name input fields
        repeat(numJugadores) { index ->
            val editText = EditText(this)
            editText.hint = "Participante ${index + 1}"
            editText.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            editText.setPadding(16)
            namesContainer.addView(editText)
        }

        backButton.setOnClickListener {
            finish()
        }

        generateButton.setOnClickListener {
            val nombres = (0 until namesContainer.childCount).map { i ->
                val et = namesContainer.getChildAt(i) as EditText
                et.text.toString().trim()
            }

            if (nombres.any { it.isBlank() }) {
                // Show error - could add a Toast or TextView message here
                return@setOnClickListener
            }

            if(checkNames(nombres)) startTournament(tournamentName, numJugadores, nombres, numSets, includeSetsResults)
        }

        generateEmptyButton.setOnClickListener {
            val nombres = (1..namesContainer.childCount).map { i -> i.toString() }
            startTournament(tournamentName, numJugadores, nombres, numSets, includeSetsResults)
        }
    }

    private fun checkNames(nombres: List<String>): Boolean {
        val uniqueNames = nombres.toSet()
        if (uniqueNames.size < nombres.size) {
            Toast.makeText(this, "No pueden haber nombres repetidos.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun startTournament(tournamentName: String, numJugadores: Int, nombres: List<String>, numSets: Int, includeSetsResults: Boolean) {
        val domainController = DomainController.getInstance()
        domainController.setTournament(domainController.createTournament(tournamentName,  numJugadores, nombres.toTypedArray(), numSets, includeSetsResults))
        val intent = Intent(this, TournamentActivity::class.java)
        startActivity(intent)
    }
}
