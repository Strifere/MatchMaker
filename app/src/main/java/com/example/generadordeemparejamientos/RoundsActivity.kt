// kotlin
package com.example.generadordeemparejamientos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class RoundsActivity : AppCompatActivity() {
    private lateinit var roundsContainer: LinearLayout
    private lateinit var tournament: Tournament
    private var totalRounds: Int = 0
    private var busquedaRonda = false
    private var busquedaParticipante = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rounds)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val tableButton = findViewById<Button>(R.id.tableButton)
        roundsContainer = findViewById(R.id.roundsContainer)
        val searchPlayerButton = findViewById<Button>(R.id.searchPlayerMatchesButton)
        val searchRoundButton = findViewById<Button>(R.id.searchRoundButton)

        tournament = intent.getSerializableExtra("tournament") as Tournament
        @Suppress("UNCHECKED_CAST")

        backButton.setOnClickListener { finish() }

        tableButton.setOnClickListener {
            val tableIntent = Intent(this, TableActivity::class.java)
            tableIntent.putExtra("tournament", tournament)
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
            roundsContainer.addView(buildRoundCard(ronda))
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
                val rondaFiltrada = Ronda(ronda.numero, filteredEmparejamientos, if (ronda.libre == playerName) playerName else null)
                roundsContainer.addView(buildRoundCard(rondaFiltrada))
            }
        }
    }

    private fun renderSingleRound(roundNumber: Int) {
        roundsContainer.removeAllViews()
        val ronda = tournament.rondas[roundNumber - 1]
        roundsContainer.addView(buildRoundCard(ronda))
    }

    private fun buildRoundCard(ronda: Ronda): View {
        val cardView = LayoutInflater.from(this).inflate(R.layout.card_round, roundsContainer, false)
        val roundTitle = cardView.findViewById<TextView>(R.id.roundTitle)
        val matchesContainer = cardView.findViewById<LinearLayout>(R.id.matchesContainer)
        val byeText = cardView.findViewById<TextView>(R.id.byeText)
        val inputResultsButton = cardView.findViewById<Button>(R.id.inputResultsButton)
        var isShowingMatches = false

        roundTitle.text = "Ronda ${ronda.numero}"

        for ((player1, player2) in ronda.emparejamientos) {
            if (!isShowingMatches) isShowingMatches = true
            val matchView = LayoutInflater.from(this).inflate(R.layout.item_match, matchesContainer, false)
            val player1View = matchView.findViewById<TextView>(R.id.player1)
            val player2View = matchView.findViewById<TextView>(R.id.player2)

            player1View.text = player1
            player2View.text = player2

            val result = ronda.resultados[Pair(player1, player2)]
            if (result != null && tournament.shouldDisplayResult(result)) {
                // Only strike through if match is finished
                if (tournament.isMatchFinished(result)) {
                    player1View.paintFlags = player1View.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                    player2View.paintFlags = player2View.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                }

                // Add result text
                val resultText = StringBuilder("(${result.player1Score}-${result.player2Score})")
                val resultView = matchView.findViewById<TextView>(R.id.resultText)
                resultView.text = resultText.toString()
                resultView.visibility = View.VISIBLE
            }

            matchesContainer.addView(matchView)
        }

        if (ronda.libre != null) {
            byeText.visibility = View.VISIBLE
            byeText.text = "Descansa: ${ronda.libre}"
        } else {
            byeText.visibility = View.GONE
        }
        if (!isShowingMatches) inputResultsButton.visibility = View.GONE
        inputResultsButton.setOnClickListener {
            showResultsDialog(ronda)
        }

        return cardView
    }

    private fun showResultsDialog(ronda: Ronda) {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_input_results, null)
        val matchesContainer = dialogView.findViewById<LinearLayout>(R.id.resultsMatchesContainer)

        for ((player1, player2) in ronda.emparejamientos) {
            val matchInputView = inflater.inflate(R.layout.item_result_input, matchesContainer, false)
            val player1Name = matchInputView.findViewById<TextView>(R.id.resultPlayer1Name)
            val player2Name = matchInputView.findViewById<TextView>(R.id.resultPlayer2Name)
            val player1Score = matchInputView.findViewById<EditText>(R.id.resultPlayer1Score)
            val player2Score = matchInputView.findViewById<EditText>(R.id.resultPlayer2Score)
            val setsContainer = matchInputView.findViewById<LinearLayout>(R.id.setsContainer)

            player1Name.text = player1
            player2Name.text = player2

            val existingResult = ronda.resultados[Pair(player1, player2)]
            if (existingResult != null) {
                player1Score.setText(existingResult.player1Score.toString())
                player2Score.setText(existingResult.player2Score.toString())
            }

            if (tournament.includeSetResults) {
                for (setIndex in 0 until tournament.bestOf) {
                    val setView = inflater.inflate(R.layout.item_set_input, setsContainer, false)
                    val setTitle = setView.findViewById<TextView>(R.id.setTitle)
                    val setPlayer1 = setView.findViewById<EditText>(R.id.setPlayer1Points)
                    val setPlayer2 = setView.findViewById<EditText>(R.id.setPlayer2Points)

                    setTitle.text = "Set ${setIndex + 1}"

                    val setNumber = setIndex + 1
                    if (existingResult != null && existingResult.sets.containsKey(setNumber)) {
                        val existingSet = existingResult.sets[setNumber]
                        setPlayer1.setText(existingSet?.player1Points.toString())
                        setPlayer2.setText(existingSet?.player2Points.toString())
                    }

                    setsContainer.addView(setView)
                }
            }

            matchesContainer.addView(matchInputView)
        }

        AlertDialog.Builder(this)
            .setTitle("Introducir resultados - Ronda ${ronda.numero}")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                saveResults(tournament, ronda, dialogView)
                renderAllRounds(tournament.rondas)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun saveResults(tournament: Tournament, ronda : Ronda, dialogView: View) {
        val matchesContainer = dialogView.findViewById<LinearLayout>(R.id.resultsMatchesContainer)

        for (i in 0 until matchesContainer.childCount) {
            val matchView = matchesContainer.getChildAt(i)
            val player1Score = matchView.findViewById<EditText>(R.id.resultPlayer1Score).text.toString().toIntOrNull() ?: 0
            val player2Score = matchView.findViewById<EditText>(R.id.resultPlayer2Score).text.toString().toIntOrNull() ?: 0

            val setResults = linkedMapOf<Int, SetResult>()
            if (tournament.includeSetResults) {
                val setsContainer = matchView.findViewById<LinearLayout>(R.id.setsContainer)
                for (j in 0 until setsContainer.childCount) {
                    val setView = setsContainer.getChildAt(j)
                    val setP1 = setView.findViewById<EditText>(R.id.setPlayer1Points).text.toString().toIntOrNull() ?: 0
                    val setP2 = setView.findViewById<EditText>(R.id.setPlayer2Points).text.toString().toIntOrNull() ?: 0
                    setResults[j + 1] = SetResult(setP1, setP2)  // Key is set number (1-indexed)
                }
            }

            val matchPair = ronda.emparejamientos[i]
            ronda.resultados[matchPair] = MatchResult(player1Score, player2Score, setResults)
        }
    }
}
