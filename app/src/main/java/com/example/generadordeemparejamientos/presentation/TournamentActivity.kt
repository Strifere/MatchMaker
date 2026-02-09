package com.example.generadordeemparejamientos.presentation

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.generadordeemparejamientos.R

class TournamentActivity : AppCompatActivity() {

    private lateinit var roundsCard: FrameLayout
    private lateinit var playersCard: FrameLayout
    private lateinit var tableCard: FrameLayout
    private lateinit var classificationCard: FrameLayout

    private lateinit var roundsIntent: Intent
    private lateinit var playersIntent: Intent
    private lateinit var tableIntent: Intent
    private lateinit var classificationIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament)

        val exitButton = findViewById<Button>(R.id.exitButton)
        exitButton.setOnClickListener { handleExit() }

        roundsCard = findViewById(R.id.roundsCard)
        playersCard = findViewById(R.id.playersCard)
        tableCard = findViewById(R.id.tableCard)
        classificationCard = findViewById(R.id.classificationCard)

        roundsIntent = Intent(this, RoundsActivity::class.java)
        playersIntent = Intent(this, PlayersActivity::class.java)
        tableIntent = Intent(this, TableActivity::class.java)
        classificationIntent = Intent(this, ClassificationActivity::class.java)

        roundsCard.setOnClickListener {
            startActivity(roundsIntent)
        }
        playersCard.setOnClickListener {
            startActivity(playersIntent)
        }
        tableCard.setOnClickListener {
            startActivity(tableIntent)
        }
        classificationCard.setOnClickListener {
            startActivity(classificationIntent)
        }
    }

    private fun handleExit() {
        AlertDialog.Builder(this)
            .setTitle("Salir del torneo")
            .setView(TextView(this).apply {
                text = "¿Estás seguro de que quieres salir del torneo? Se perderán todos los datos no guardados."
                setPadding(50, 40, 50, 40)
            })
            .setPositiveButton("Sí") { _, _ -> finish() }
            .setNegativeButton("No", null)
            .show()
    }
}