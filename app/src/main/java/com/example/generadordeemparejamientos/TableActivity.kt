package com.example.generadordeemparejamientos

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt

class TableActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

        val tournament = intent.getSerializableExtra("tournament") as Tournament
        val numJugadores = tournament.numJugadores
        val nombres = tournament.nombres
        @Suppress("UNCHECKED_CAST")
        val tabla = tournament.tabla
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
                val matchResult = findMatchResult(rondas, player1Name, player2Name)

                cell.text = when {
                    i == j -> "-"  // Diagonal cells
                    matchResult != null && isMatchFinished(matchResult) -> {
                        // Display result from perspective of player i (row player)
                        "${matchResult.player1Score}/${matchResult.player2Score}"
                    }
                    matchResult != null -> "/"  // Match exists but not finished
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

    private fun isMatchFinished(result: MatchResult): Boolean {
        // If both scores are 0, match hasn't started
        return !(result.player1Score == 0 && result.player2Score == 0)
    }
}
