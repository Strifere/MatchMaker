package com.example.generadordeemparejamientos

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.graphics.Color
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt

class ClassificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classification)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

        val tournament = TournamentApplication.getTournament() as Tournament
        val numJugadores = tournament.numJugadores
        val nombres = tournament.nombres
        @Suppress("UNCHECKED_CAST")
        val rondas = tournament.rondas

        backButton.setOnClickListener {
            finish()
        }

        // Detect dark mode
        val isDarkMode = isDarkModeEnabled()

        // Get theme-aware colors
        val headerBackgroundColor = ContextCompat.getColor(this, android.R.color.darker_gray)
        val headerTextColor = ContextCompat.getColor(this, android.R.color.white)
        val diagonalColor = ContextCompat.getColor(this, android.R.color.holo_orange_light)
        val cellBackgroundColor = if (isDarkMode) "#1F1F1F".toColorInt() else Color.WHITE
        val cellTextColor = if (isDarkMode) Color.WHITE else Color.BLACK

        // Header row with player names
        val headerRow = TableRow(this)
        headerRow.setBackgroundColor(headerBackgroundColor)

        // Top-left corner (empty)
        val emptyCell = TextView(this)
        emptyCell.text = ""
        emptyCell.setPadding(8, 8, 8, 8)
        emptyCell.setBackgroundColor(headerBackgroundColor)
        emptyCell.setTextColor(headerTextColor)
        headerRow.addView(emptyCell)

        // Player names as column headers
        for (nombre in nombres) {
            val headerCell = TextView(this)
            headerCell.text = nombre
            headerCell.setPadding(8, 8, 8, 8)
            headerCell.textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
            headerCell.setBackgroundColor(headerBackgroundColor)
            headerCell.setTextColor(headerTextColor)
            headerCell.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12f)
            headerRow.addView(headerCell)
        }
        tableLayout.addView(headerRow)

        // Data rows
        for (i in 0 until numJugadores) {
            val row = TableRow(this)

            // Row header (player name)
            val rowHeader = TextView(this)
            rowHeader.text = nombres[i]
            rowHeader.setPadding(8, 8, 8, 8)
            rowHeader.setBackgroundColor(headerBackgroundColor)
            rowHeader.setTextColor(headerTextColor)
            rowHeader.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12f)
            row.addView(rowHeader)

            // Data cells
            for (j in 0 until numJugadores) {
                val cell = TextView(this)

                // Get match result if available
                val player1Name = nombres[i]
                val player2Name = nombres[j]
                val matchResultSelf = findMatchResult(rondas, player1Name, player2Name)
                val matchResultAgainst = findMatchResult(rondas, player2Name, player1Name)

                cell.text = when {
                    i == j -> "-"  // Diagonal cells
                    matchResultSelf != null && matchResultSelf.shouldDisplayResult()-> {
                        // Display result from perspective of player i (row player)
                        "${matchResultSelf.player1Score}/${matchResultSelf.player2Score}"
                    }
                    matchResultAgainst != null && matchResultAgainst.shouldDisplayResult() -> {
                        // Display result from perspective of player j (column player)
                        "${matchResultAgainst.player2Score}/${matchResultAgainst.player1Score}"
                    }
                    else -> ""  // No match yet
                }

                cell.setPadding(8, 8, 8, 8)
                cell.textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
                cell.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 11f)

                // Apply colors based on cell type
                if (i == j) {
                    // Diagonal cells (highlighted)
                    cell.setBackgroundColor(diagonalColor)
                    cell.setTextColor(Color.BLACK)
                } else {
                    // Regular data cells
                    cell.setBackgroundColor(cellBackgroundColor)
                    cell.setTextColor(cellTextColor)

                    // Make cell clickable to enter/edit match results
                    cell.isClickable = true
                    cell.isFocusable = true
                    cell.setOnClickListener {
                        showMatchResultDialog(player1Name, player2Name, matchResultSelf ?: matchResultAgainst)
                    }
                }
                row.addView(cell)
            }
            tableLayout.addView(row)
        }
    }

    private fun isDarkModeEnabled(): Boolean {
        val nightModeFlags = this.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    private fun findMatchResult(rondas: List<Ronda>, player1Name: String, player2Name: String): MatchResult? {
        for (ronda in rondas) {
            val result = ronda.resultados[Pair(player1Name, player2Name)]
            if (result != null) {
                return result
            }
        }
        return null
    }

    private fun showMatchResultDialog(player1Name: String, player2Name: String, existingResult: MatchResult?) {
        // Get tournament from singleton
        val tournament = TournamentApplication.getTournament() as Tournament
        @Suppress("UNCHECKED_CAST")
        val rondas = tournament.rondas

        var targetRonda: Ronda? = null
        var matchPair: Pair<String, String>? = null

        // Search for the match in the rounds
        for (ronda in rondas) {
            for (pareja in ronda.emparejamientos) {
                if ((pareja.first == player1Name && pareja.second == player2Name) ||
                    (pareja.first == player2Name && pareja.second == player1Name)) {
                    targetRonda = ronda
                    matchPair = pareja
                    break
                }
            }
            if (targetRonda != null) break
        }

        if (targetRonda == null || matchPair == null) {
            android.widget.Toast.makeText(this, "Este emparejamiento no existe en el torneo", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_input_results, null)
        val matchesContainer = dialogView.findViewById<LinearLayout>(R.id.resultsMatchesContainer)

        // Create the match input view
        val matchInputView = inflater.inflate(R.layout.item_result_input, matchesContainer, false)
        val setInputLayout = matchInputView.findViewById<LinearLayout>(R.id.setLayout)
        val player1NameView = matchInputView.findViewById<TextView>(R.id.resultPlayer1Name)
        val player2NameView = matchInputView.findViewById<TextView>(R.id.resultPlayer2Name)
        val player1Score = matchInputView.findViewById<EditText>(R.id.resultPlayer1Score)
        val player2Score = matchInputView.findViewById<EditText>(R.id.resultPlayer2Score)
        val setsContainer = matchInputView.findViewById<LinearLayout>(R.id.setsContainer)

        player1NameView.text = matchPair.first
        player2NameView.text = matchPair.second

        // Pre-fill with existing result if available
        val currentResult = targetRonda.resultados[matchPair]
        if (currentResult != null) {
            player1Score.setText(currentResult.player1Score.toString())
            player2Score.setText(currentResult.player2Score.toString())
        }

        // Add set inputs if tournament includes set results
        if (tournament.includeSetResults) {
            setInputLayout.visibility = View.GONE
            for (setIndex in 0 until tournament.bestOf) {
                val setView = inflater.inflate(R.layout.item_set_input, setsContainer, false)
                val setTitle = setView.findViewById<TextView>(R.id.setTitle)
                val setPlayer1 = setView.findViewById<EditText>(R.id.setPlayer1Points)
                val setPlayer2 = setView.findViewById<EditText>(R.id.setPlayer2Points)

                setTitle.text = "Set ${setIndex + 1}"

                val setNumber = setIndex + 1
                if (currentResult != null && currentResult.sets.containsKey(setNumber)) {
                    val existingSet = currentResult.sets[setNumber]
                    setPlayer1.setText(existingSet?.player1Points.toString())
                    setPlayer2.setText(existingSet?.player2Points.toString())
                }

                setsContainer.addView(setView)
            }
        }

        matchesContainer.addView(matchInputView)

        // Show dialog
        val finalRonda = targetRonda
        val finalPair = matchPair
        AlertDialog.Builder(this)
            .setTitle("Resultado del partido - Ronda ${finalRonda.numero}")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                saveMatchResult(tournament, finalRonda, finalPair, matchInputView)
                // Refresh the table
                recreate()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun saveMatchResult(tournament: Tournament, ronda: Ronda, matchPair: Pair<String, String>, matchView: android.view.View) {
        val player1Score = matchView.findViewById<EditText>(R.id.resultPlayer1Score).text.toString().toIntOrNull() ?: 0
        val player2Score = matchView.findViewById<EditText>(R.id.resultPlayer2Score).text.toString().toIntOrNull() ?: 0

        val setResults = linkedMapOf<Int, SetResult>()
        if (tournament.includeSetResults) {
            val setsContainer = matchView.findViewById<LinearLayout>(R.id.setsContainer)
            for (j in 0 until setsContainer.childCount) {
                val setView = setsContainer.getChildAt(j)
                val setP1 = setView.findViewById<EditText>(R.id.setPlayer1Points).text.toString().toIntOrNull() ?: 0
                val setP2 = setView.findViewById<EditText>(R.id.setPlayer2Points).text.toString().toIntOrNull() ?: 0
                setResults[j] = SetResult(setP1, setP2)
            }
        }

        val result = MatchResult(player1Score, player2Score, tournament.includeSetResults,setResults)
        // Save the result using the original match pair
        if (result.checkMatchResult(tournament.bestOf)) ronda.resultados[matchPair] = result
        else {
            android.widget.Toast.makeText(this, "Resultado inválido según las reglas del torneo.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
