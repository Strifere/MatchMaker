package com.example.matchmaker.presentation

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.example.matchmaker.R
import com.example.matchmaker.domain.controllers.DomainController
import com.example.matchmaker.domain.classes.Match
import com.example.matchmaker.domain.classes.Player
import com.example.matchmaker.domain.classes.Round
import com.example.matchmaker.domain.classes.Tournament
import com.example.matchmaker.utils.isDarkModeEnabled
import com.example.matchmaker.utils.showMatchInputDialog
import kotlinx.coroutines.launch

class TableActivity : AppCompatActivity() {
    private lateinit var nombres : Array<String>
    private var numJugadores = -1
    private lateinit var tableLayout: TableLayout
    private lateinit var tournament: Tournament
    private lateinit var rounds: List<Round>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        tableLayout = findViewById(R.id.tableLayout)

        tournament = DomainController.getInstance().getTournament() as Tournament
        numJugadores = tournament.numJugadores
        nombres = tournament.nombres
        rounds = tournament.rounds
        @Suppress("UNCHECKED_CAST")

        backButton.setOnClickListener {
            finish()
        }

        // Detect dark mode
        val isDarkMode = isDarkModeEnabled(this)

        // Get theme-aware colors
        val headerBackgroundColor = ContextCompat.getColor(this, android.R.color.darker_gray)
        val headerTextColor = ContextCompat.getColor(this, android.R.color.white)
        val diagonalColor = ContextCompat.getColor(this, android.R.color.holo_orange_light)
        val cellBackgroundColor = if (isDarkMode) {
            "#1F1F1F".toColorInt()  // Dark gray for dark mode
        } else {
            Color.WHITE
        }
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
            headerCell.textAlignment = View.TEXT_ALIGNMENT_CENTER
            headerCell.setBackgroundColor(headerBackgroundColor)
            headerCell.setTextColor(headerTextColor)
            headerCell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
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
            rowHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            row.addView(rowHeader)

            // Data cells
            for (j in 0 until numJugadores) {
                val cell = TextView(this)

                // Get match result if available
                val player1Name = nombres[i]
                val player2Name = nombres[j]
                val matchResultSelf = findMatchResult(rounds, player1Name, player2Name)
                val matchResultAgainst = findMatchResult(rounds, player2Name, player1Name)

                cell.text = when {
                    i == j -> "-"  // Diagonal cells
                    matchResultSelf != null && matchResultSelf.shouldDisplayResult()-> {
                        // Display result from perspective of player i (row player)
                        "${matchResultSelf.player1Sets}/${matchResultSelf.player2Sets}"
                    }
                    matchResultAgainst != null && matchResultAgainst.shouldDisplayResult() -> {
                        // Display result from perspective of player j (column player)
                        "${matchResultAgainst.player2Sets}/${matchResultAgainst.player1Sets}"
                    }
                    else -> ""  // No match yet
                }

                cell.setPadding(8, 8, 8, 8)
                cell.textAlignment = View.TEXT_ALIGNMENT_CENTER
                cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)

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
                        showMatchResultDialog(player1Name, player2Name)
                    }
                }
                row.addView(cell)
            }
            tableLayout.addView(row)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DomainController.getInstance().saveTournament(this, false)
    }

    private fun findMatchResult(rounds: List<Round>, player1Name: String, player2Name: String): Match? {
        for (ronda in rounds) {
            val match = ronda.getMatchByNames(player1Name, player2Name)
            if (match != null) {
                return match
            }
        }
        return null
    }

    private fun showMatchResultDialog(player1Name: String, player2Name: String) {
        var targetRound: Round? = null
        var truePlayer1: Player? = null
        var truePlayer2: Player? = null

        // Search for the match in the rounds
        for (round in rounds) {
            for (match in round.matches) {
                if ((match.player1.name == player1Name && match.player2.name == player2Name) ||
                    (match.player1.name == player2Name && match.player2.name == player1Name)) {
                    targetRound = round
                    truePlayer1 = match.player1
                    truePlayer2 = match.player2
                    break
                }
            }
            if (targetRound != null) break
        }

        if (targetRound == null) {
            Toast.makeText(this, "Este emparejamiento no existe en el torneo", Toast.LENGTH_SHORT).show()
            return
        } else {
            val context = this
            lifecycleScope.launch {
                if (showMatchInputDialog(
                        tournament,
                        targetRound,
                        truePlayer1!!,
                        truePlayer2!!,
                        context
                    )
                ) {
                    // Refresh the table
                    recreate()
                }
            }
        }
    }
}
