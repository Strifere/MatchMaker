// kotlin
package com.example.generadordeemparejamientos.presentation

import android.graphics.Paint
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.generadordeemparejamientos.R
import com.example.generadordeemparejamientos.domain.controllers.DomainController
import com.example.generadordeemparejamientos.domain.classes.Round
import com.example.generadordeemparejamientos.domain.classes.Tournament
import com.example.generadordeemparejamientos.utils.showMatchInputDialog
import kotlinx.coroutines.launch

class RoundsActivity : AppCompatActivity() {
    private lateinit var roundsContainer: LinearLayout
    private lateinit var tournament: Tournament
    private lateinit var searchPlayerButton : Button
    private lateinit var searchRoundButton : Button
    private var totalRounds: Int = 0
    private var busquedaRonda = false
    private var currentRoundIndex = -1
    private var busquedaParticipante = false
    private var currentPlayerIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rounds)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        roundsContainer = findViewById(R.id.roundsContainer)
        searchPlayerButton = findViewById(R.id.searchPlayerMatchesButton)
        searchRoundButton = findViewById(R.id.searchRoundButton)

        tournament = DomainController.getInstance().getTournament() as Tournament

        backButton.setOnClickListener {
            finish()
        }

        searchPlayerButton.setOnClickListener {
            if (!busquedaParticipante) showPlayerSearchDialog(searchPlayerButton, searchRoundButton)
            else {
                renderAllRounds(tournament.rounds)
                busquedaParticipante = false
                searchRoundButton.visibility = View.VISIBLE
                searchPlayerButton.text = "Buscar partidos participante"
            }
        }

        searchRoundButton.setOnClickListener {
            if (!busquedaRonda) showRoundSearchDialog(searchRoundButton, searchPlayerButton)
            else {
                renderAllRounds(tournament.rounds)
                busquedaRonda = false
                currentRoundIndex = -1
                searchPlayerButton.visibility = View.VISIBLE
                searchRoundButton.text = "Buscar ronda"
            }
        }

        // Check if we came from PlayersActivity with a player name to search for
        val playerName = intent.getStringExtra("playerName")
        if (playerName != null) {
            val playerIndex = tournament.nombres.indexOfFirst { it.equals(playerName, ignoreCase = true) }
            if (playerIndex != -1) {
                currentPlayerIndex = playerIndex
                busquedaParticipante = true
                busquedaRonda = false
                currentRoundIndex = -1
                searchRoundButton.visibility = View.GONE
                searchPlayerButton.text = "Cancelar Búsqueda"
            } else {
                Toast.makeText(this, "Participante no encontrado.", Toast.LENGTH_SHORT).show()
            }
        }

        totalRounds = tournament.rounds.size
        refreshRounds()
    }

    override fun onResume() {
        super.onResume()
        refreshRounds()
    }
    private fun refreshRounds() {
        // Refresh the rounds data from the tournament singleton in case it was updated in TableActivity
        tournament = DomainController.getInstance().getTournament() as Tournament
        totalRounds = tournament.rounds.size
        if (busquedaParticipante && currentPlayerIndex != -1) {
            renderContestantRounds(currentPlayerIndex)
        } else if (busquedaRonda && currentRoundIndex != -1) {
            renderSingleRound(currentRoundIndex)
        } else {
            renderAllRounds(tournament.rounds)
        }
    }

    private fun showRoundSearchDialog(searchRoundButton: Button, searchPlayerButton: Button) {
        if (tournament.rounds.isEmpty()) {
            Toast.makeText(this, "No hay rondas calculadas.", Toast.LENGTH_SHORT).show()
            return
        }

        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "Introduce el número de ronda (1..$totalRounds)"
            setPadding(40,30,40,30)
        }

        AlertDialog.Builder(this)
            .setTitle("Buscar Ronda")
            .setView(input)
            .setPositiveButton("Buscar") { _, _ ->
                val text = input.text?.toString()?.trim()
                val round = (text?.toIntOrNull())?.minus(1) // Convert to 0-based index
                if (round == null || round < 0 || round >= totalRounds) {
                    Toast.makeText(this, "Ronda Inválida. Tiene que ser entre 1..$totalRounds.", Toast.LENGTH_SHORT).show()
                } else {
                    renderSingleRound(round)
                    busquedaRonda = true
                    currentRoundIndex = round
                    searchPlayerButton.visibility = View.GONE
                    searchRoundButton.text = "Cancelar Búsqueda"
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showPlayerSearchDialog(searchPlayerButton: Button, searchRoundButton: Button) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
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
                        currentRoundIndex = -1
                        searchRoundButton.visibility = View.GONE
                        searchPlayerButton.text = "Cancelar Búsqueda"
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun renderAllRounds(rounds: List<Round>) {
        roundsContainer.removeAllViews()
        for (ronda in rounds) {
            roundsContainer.addView(buildRoundCard(ronda, true, null))
        }
    }

    private fun renderContestantRounds (playerIndex: Int) {
        roundsContainer.removeAllViews()
        val playerName = tournament.nombres[playerIndex]
        for (ronda in tournament.rounds) {
            val filteredEmparejamientos = ronda.emparejamientos.filter {
                it.first == playerName || it.second == playerName
            }
            if (filteredEmparejamientos.isNotEmpty() || ronda.libre?.name == playerName) {
                roundsContainer.addView(buildRoundCard(ronda, false, playerName))
            }
        }
    }

    private fun renderSingleRound(roundNumber: Int) {
        roundsContainer.removeAllViews()
        val ronda = tournament.rounds[roundNumber]
        roundsContainer.addView(buildRoundCard(ronda, true, null))
    }

    private fun buildRoundCard(round: Round, full: Boolean, playerName: String?) : View {
        val cardView = LayoutInflater.from(this).inflate(R.layout.card_round, roundsContainer, false)
        val roundTitle = cardView.findViewById<TextView>(R.id.roundTitle)
        val matchesContainer = cardView.findViewById<LinearLayout>(R.id.matchesContainer)
        val byeText = cardView.findViewById<TextView>(R.id.byeText)
        var isShowingMatches = false

        roundTitle.text = "Ronda ${round.numero}"

        for (match in round.matches) {
            val player1 = match.player1.name
            val player2 = match.player2.name
            if (!full && player1 != playerName && player2 != playerName) continue
            if (!isShowingMatches) isShowingMatches = true
            val matchView = LayoutInflater.from(this).inflate(R.layout.item_match, matchesContainer, false)
            val player1View = matchView.findViewById<TextView>(R.id.player1)
            val player2View = matchView.findViewById<TextView>(R.id.player2)

            player1View.text = player1
            player2View.text = player2

            if (match.shouldDisplayResult()) {
                // Only strike through if match is finished
                if (match.isMatchFinished(tournament.bestOf)) {
                    player1View.paintFlags = player1View.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    player2View.paintFlags = player2View.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }

                // Add result text
                val resultText = StringBuilder("(${match.player1Sets}-${match.player2Sets})")
                val resultView = matchView.findViewById<TextView>(R.id.resultText)
                resultView.text = resultText.toString()
                resultView.visibility = View.VISIBLE
            }
            val matchCard = matchView.findViewById<FrameLayout>(R.id.matchCard)
            matchCard.setOnClickListener {
                val context = this
                lifecycleScope.launch {
                    if (showMatchInputDialog(tournament, round, match.player1, match.player2, context)) {
                        refreshRounds()
                    }
                }
            }
            matchesContainer.addView(matchView)
        }

        if (round.libre != null) {
            byeText.visibility = View.VISIBLE
            byeText.text = "Descansa: ${round.libre.name}"
        } else {
            byeText.visibility = View.GONE
        }
        return cardView
    }
}
