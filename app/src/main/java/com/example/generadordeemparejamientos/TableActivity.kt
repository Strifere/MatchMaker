package com.example.generadordeemparejamientos

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.graphics.Color
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class TableActivity : AppCompatActivity() {
    private lateinit var nombres : Array<String>
    private var numJugadores = -1
    private lateinit var tableLayout: TableLayout
    private lateinit var tournament: Tournament
    private lateinit var rondas: List<Ronda>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val classificationButton = findViewById<Button>(R.id.classificationButton)
        tableLayout = findViewById<TableLayout>(R.id.tableLayout)

        tournament = TournamentApplication.getTournament() as Tournament
        numJugadores = tournament.numJugadores
        nombres = tournament.nombres
        rondas = tournament.rondas
        @Suppress("UNCHECKED_CAST")

        backButton.setOnClickListener {
            finish()
        }
        classificationButton.setOnClickListener {
            val classificationIntent = android.content.Intent(this, ClassificationActivity::class.java)
            startActivity(classificationIntent)
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
                        showMatchResultDialog(player1Name, player2Name)
                    }
                }
                row.addView(cell)
            }
            tableLayout.addView(row)
        }
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

    private fun showMatchResultDialog(player1Name: String, player2Name: String) {
        val rondas = rondas

        var targetRonda: Ronda? = null
        var player1TrueName: String? = null
        var player2TrueName: String? = null

        // Search for the match in the rounds
        for (ronda in rondas) {
            for (pareja in ronda.emparejamientos) {
                if ((pareja.first == player1Name && pareja.second == player2Name) ||
                    (pareja.first == player2Name && pareja.second == player1Name)) {
                    targetRonda = ronda
                    player1TrueName = pareja.first
                    player2TrueName = pareja.second
                    break
                }
            }
            if (targetRonda != null) break
        }

        if (targetRonda == null) {
            android.widget.Toast.makeText(this, "Este emparejamiento no existe en el torneo", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val context = this
        lifecycleScope.launch {
            if (showMatchInputDialog(tournament, targetRonda, player1TrueName!!, player2TrueName!!, context)) {
                // Refresh the table
                recreate()
            }
        }
    }
}
