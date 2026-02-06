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
import java.util.SortedMap
import java.util.SortedSet

class ClassificationActivity : AppCompatActivity() {
    private lateinit var tournament: Tournament

    private class PlayerStats(
        var pj: Int = 0,
        var pg: Int = 0,
        var pp: Int = 0,
        var sf: Int = 0,
        var sc: Int = 0,
        var pts: Int = 0
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classification)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

        tournament = TournamentApplication.getTournament() as Tournament
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

        val headers = arrayOf("", "Participante", "PJ", "PG", "PP", "SF", "SC", "Pts")
        var data : Array<Array<String>> = computeClassificationData()

        val headerRow = TableRow(this)
        headerRow.setBackgroundColor(headerBackgroundColor)

        for (header in headers) {
            val headerCell = TextView(this)
            headerCell.text = header
            headerCell.setPadding(8, 8, 8, 8)
            headerCell.textAlignment = View.TEXT_ALIGNMENT_CENTER
            headerCell.setBackgroundColor(headerBackgroundColor)
            headerCell.setTextColor(headerTextColor)
            headerCell.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12f)
            headerRow.addView(headerCell)
        }
        tableLayout.addView(headerRow)

        for (row in data) {
            val tableRow = TableRow(this)
            for (cell in row) {
                val cellView = TextView(this)
                cellView.text = cell
                cellView.setPadding(8, 8, 8, 8)
                cellView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                cellView.setBackgroundColor(cellBackgroundColor)
                cellView.setTextColor(cellTextColor)
                tableRow.addView(cellView)
            }
            tableLayout.addView(tableRow)
        }
    }

    private fun computeClassificationData(): Array<Array<String>> {
        val table : Array<Array<String>> = Array(tournament.numJugadores) { Array(8) { "" } }

        // here we will compute the stats for each player based on the match results in tournament.rondas
        val classification : SortedSet<String> = sortedSetOf()
        val stats : SortedMap<String, PlayerStats> = initializeStats()
        for (ronda in tournament.rondas) {
            for (partido in ronda.resultados) {
                val player1name = partido.key.first
                val player2name = partido.key.second
                val result = partido.value
                val player1score = result.player1Score
                val player2score = result.player2Score
                // Update matches played + 1 for both players
                stats[player1name]?.pj += 1
                stats[player2name]?.pj += 1
                // Update sets for and against
                stats[player1name]?.sf += player1score
                stats[player1name]?.sc += player2score
                stats[player2name]?.sf += player2score
                stats[player2name]?.sc += player1score

                if (result.isMatchFinished(tournament.bestOf)) {
                    if (player1score > player2score) {
                        // player 1 wins
                        stats[player1name]?.pg += 1
                        stats[player2name]?.pp += 1
                        stats[player1name]?.pts += 2
                    } else if (player2score > player1score) {
                        // player 2 wins
                        stats[player2name]?.pg += 1
                        stats[player1name]?.pp += 1
                        stats[player2name]?.pts += 2
                    }
                }
            }
        }

        for ((playerName, playerStats) in stats) {
            insertIntoClassification(classification, playerName, playerStats)
        }

        for (i in 0 until classification.size) {
            val playerName = classification.last().substringAfter(":")
            classification.remove(classification.last())
            val playerStats = stats[playerName] ?: PlayerStats()
            table[i] = arrayOf(
                (i+1).toString(),
                playerName,
                playerStats.pj.toString(),
                playerStats.pg.toString(),
                playerStats.pp.toString(),
                playerStats.sf.toString(),
                playerStats.sc.toString(),
                playerStats.pts.toString()
            )
        }

        return table
    }

    private fun initializeStats(): SortedMap<String, PlayerStats> {
        val stats : SortedMap<String, PlayerStats> = sortedMapOf()
        for (playerName in tournament.nombres) {
            stats[playerName] = PlayerStats()
        }
        return stats
    }

    private fun insertIntoClassification(classification: SortedSet<String>, playerName: String, playerStats: PlayerStats) {
        // We will sort the classification by points, then by set difference, then by sets for, and finally alphabetically
        val playerEntry = "${playerStats.pts}-${playerStats.pg}${playerStats.sf}-${playerStats.pp}-${playerStats.sf - playerStats.sc}:$playerName"
        classification.add(playerEntry)
     }
}