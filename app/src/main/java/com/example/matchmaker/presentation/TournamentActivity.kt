package com.example.matchmaker.presentation

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.matchmaker.R
import com.example.matchmaker.domain.controllers.DomainController
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class TournamentActivity : AppCompatActivity() {

    private lateinit var roundsCard: LinearLayout
    private lateinit var playersCard: LinearLayout
    private lateinit var tableCard: LinearLayout
    private lateinit var classificationCard: LinearLayout

    private lateinit var tournamentTitle: TextView
    private lateinit var editTitleButton: ImageButton

    private val domainController = DomainController.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament)

        val exitButton = findViewById<Button>(R.id.exitButton)
        exitButton.setOnClickListener { handleExit() }

        roundsCard = findViewById(R.id.roundsCard)
        playersCard = findViewById(R.id.playersCard)
        tableCard = findViewById(R.id.tableCard)
        classificationCard = findViewById(R.id.classificationCard)
        tournamentTitle = findViewById(R.id.tournamentTitle)
        editTitleButton = findViewById(R.id.editTitleButton)

        roundsCard.setOnClickListener {
            startActivity(Intent(this, RoundsActivity::class.java))
        }
        playersCard.setOnClickListener {
            startActivity(Intent(this, PlayersActivity::class.java))
        }
        tableCard.setOnClickListener {
            startActivity(Intent(this, TableActivity::class.java))
        }
        classificationCard.setOnClickListener {
            startActivity(Intent(this, ClassificationActivity::class.java))
        }

        tournamentTitle.text = domainController.getTournament()?.name

        editTitleButton.setOnClickListener {
            val input = EditText(this).apply {
                inputType = android.text.InputType.TYPE_CLASS_TEXT
                setText(tournamentTitle.text)
                setSelection(text.length) // Move cursor to the end
            }

            AlertDialog.Builder(this)
                .setTitle("Cambiar título del torneo")
                .setView(input)
                .setPositiveButton("Confirmar") { _, _ ->
                    val newName = input.text.toString().trim()
                    if (newName.isBlank()) {
                        Toast.makeText(this, "El título no puede estar vacío", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    lifecycleScope.launch {
                        val nameExists = domainController.getTournamentsByName(newName) != null
                        if (nameExists) {
                            Toast.makeText(this@TournamentActivity, "Ya existe un torneo con ese nombre. Por favor, elija otro.", Toast.LENGTH_SHORT).show()
                            cancel()
                        } else {
                            val updated = domainController.updateTournamentName(newName)
                            if (updated) {
                                Toast.makeText(this@TournamentActivity, "Título cambiado a $newName", Toast.LENGTH_SHORT).show()
                                tournamentTitle.text = newName
                            } else {
                                Toast.makeText(this@TournamentActivity, "No se pudo actualizar el nombre del torneo", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DomainController.getInstance().saveTournament(this, true)
    }
    private fun handleExit() {
        AlertDialog.Builder(this)
            .setTitle("Salir del torneo")
            .setView(TextView(this).apply {
                text = "¿Estás seguro de que quieres salir del torneo?"
                setPadding(50, 40, 50, 40)
            })
            .setPositiveButton("Sí") { _, _ ->
                DomainController.getInstance().saveTournament(this, true)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }
}