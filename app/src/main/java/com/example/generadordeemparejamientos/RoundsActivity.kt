// kotlin
package com.example.generadordeemparejamientos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class RoundsActivity : AppCompatActivity() {
    private lateinit var roundsContainer: LinearLayout
    private lateinit var tournament: Tournament
    private var totalRounds: Int = 0
    private var busquedaRonda = false
    private var busquedaParticipante = false
    private var currentPlayerIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rounds)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val tableButton = findViewById<Button>(R.id.tableButton)
        roundsContainer = findViewById(R.id.roundsContainer)
        val searchPlayerButton = findViewById<Button>(R.id.searchPlayerMatchesButton)
        val searchRoundButton = findViewById<Button>(R.id.searchRoundButton)

        tournament = TournamentApplication.getTournament() as Tournament

        backButton.setOnClickListener { finish() }

        tableButton.setOnClickListener {
            val tableIntent = Intent(this, TableActivity::class.java)
            startActivity(tableIntent)
        }

        searchPlayerButton.setOnClickListener {
            if (!busquedaParticipante) showPlayerSearchDialog(searchPlayerButton, searchRoundButton)
            else {
                renderAllRounds(tournament.rondas)
                busquedaParticipante = false
                searchRoundButton.visibility = View.VISIBLE
                searchPlayerButton.text = "Buscar partidos participante"
            }
        }

        searchRoundButton.setOnClickListener {
            if (!busquedaRonda) showRoundSearchDialog(searchRoundButton, searchPlayerButton)
            else {
                renderAllRounds(tournament.rondas)
                busquedaRonda = false
                searchPlayerButton.visibility = View.VISIBLE
                searchRoundButton.text = "Buscar ronda"
            }
        }

        totalRounds = tournament.rondas.size
        renderAllRounds(tournament.rondas)
    }

    private fun showRoundSearchDialog(searchRoundButton: Button, searchPlayerButton: Button) {
        if (tournament.rondas.isEmpty()) {
            Toast.makeText(this, "No hay rondas calculadas.", Toast.LENGTH_SHORT).show()
            return
        }

        val input = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            hint = "Introduce el número de ronda (1..$totalRounds)"
            setPadding(40,30,40,30)
        }

        AlertDialog.Builder(this)
            .setTitle("Buscar Ronda")
            .setView(input)
            .setPositiveButton("Buscar") { _, _ ->
                val text = input.text?.toString()?.trim()
                val round = text?.toIntOrNull()
                if (round == null || round < 1 || round > totalRounds) {
                    Toast.makeText(this, "Ronda Inválida. Tiene que ser entre 1..$totalRounds.", Toast.LENGTH_SHORT).show()
                } else {
                    renderSingleRound(round)
                    busquedaRonda = true
                    searchPlayerButton.visibility = View.GONE
                    searchRoundButton.text = "Cancelar Búsqueda"
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showPlayerSearchDialog(searchPlayerButton: Button, searchRoundButton: Button) {
        val input = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            hint = "Introduce el nombre del jugador"
            setPadding(40, 30, 40, 30)
        }

        AlertDialog.Builder(this)
            .setTitle("Buscar partidos de participante")
            .setView(input)
            .setPositiveButton("Buscar") { _, _ ->
                val playerName = input.text?.toString()?.trim()
                if (playerName.isNullOrBlank()) {
                    Toast.makeText(this, "Nombre inválido.", Toast.LENGTH_SHORT).show()
                } else {
                    val playerIndex = tournament.nombres.indexOfFirst { it.equals(playerName, ignoreCase = true) }
                    if (playerIndex == -1) {
                        Toast.makeText(this, "Participante no encontrado.", Toast.LENGTH_SHORT).show()
                    } else {
                        currentPlayerIndex = playerIndex
                        renderContestantRounds(playerIndex)
                        busquedaParticipante = true
                        busquedaRonda = false
                        searchRoundButton.visibility = View.GONE
                        searchPlayerButton.text = "Cancelar Búsqueda"
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun renderAllRounds(rondas: List<Ronda>) {
        roundsContainer.removeAllViews()
        for (ronda in rondas) {
            roundsContainer.addView(buildRoundCard(ronda, true, null))
        }
    }

    private fun renderContestantRounds (playerIndex: Int) {
        roundsContainer.removeAllViews()
        val playerName = tournament.nombres[playerIndex]
        for (ronda in tournament.rondas) {
            val filteredEmparejamientos = ronda.emparejamientos.filter {
                it.first == playerName || it.second == playerName
            }
            if (filteredEmparejamientos.isNotEmpty() || ronda.libre == playerName) {
                roundsContainer.addView(buildRoundCard(ronda, false, playerName))
            }
        }
    }

    private fun renderSingleRound(roundNumber: Int) {
        roundsContainer.removeAllViews()
        val ronda = tournament.rondas[roundNumber - 1]
        roundsContainer.addView(buildRoundCard(ronda, true, null))
    }

    private fun buildRoundCard(ronda: Ronda, full: Boolean, playerName: String?) : View {
        val cardView = LayoutInflater.from(this).inflate(R.layout.card_round, roundsContainer, false)
        val roundTitle = cardView.findViewById<TextView>(R.id.roundTitle)
        val matchesContainer = cardView.findViewById<LinearLayout>(R.id.matchesContainer)
        val byeText = cardView.findViewById<TextView>(R.id.byeText)
        var isShowingMatches = false

        roundTitle.text = "Ronda ${ronda.numero}"

        for ((player1, player2) in ronda.emparejamientos) {
            if (!full && player1 != playerName && player2 != playerName) continue
            if (!isShowingMatches) isShowingMatches = true
            val matchView = LayoutInflater.from(this).inflate(R.layout.item_match, matchesContainer, false)
            val player1View = matchView.findViewById<TextView>(R.id.player1)
            val player2View = matchView.findViewById<TextView>(R.id.player2)

            player1View.text = player1
            player2View.text = player2

            val result = ronda.resultados[Pair(player1, player2)]
            if (result != null && result.shouldDisplayResult()) {
                // Only strike through if match is finished
                if (result.isMatchFinished(tournament.bestOf)) {
                    player1View.paintFlags = player1View.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                    player2View.paintFlags = player2View.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                }

                // Add result text
                val resultText = StringBuilder("(${result.player1Score}-${result.player2Score})")
                val resultView = matchView.findViewById<TextView>(R.id.resultText)
                resultView.text = resultText.toString()
                resultView.visibility = View.VISIBLE
            }
            val addMatchResultButton = matchView.findViewById<ImageButton>(R.id.addMatchResultButton)
            addMatchResultButton.setOnClickListener {
                val context = this
                lifecycleScope.launch {
                    if (showMatchInputDialog(tournament, ronda, player1, player2, context)) {
                        if (busquedaParticipante) renderContestantRounds(currentPlayerIndex)
                        else if (busquedaRonda) renderSingleRound(ronda.numero)
                        else renderAllRounds(tournament.rondas)
                    }
                }
            }
            matchesContainer.addView(matchView)
        }

        if (ronda.libre != null) {
            byeText.visibility = View.VISIBLE
            byeText.text = "Descansa: ${ronda.libre}"
        } else {
            byeText.visibility = View.GONE
        }
        return cardView
    }
}
