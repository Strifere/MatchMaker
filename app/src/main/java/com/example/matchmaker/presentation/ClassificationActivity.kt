package com.example.matchmaker.presentation

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.example.matchmaker.R
import com.example.matchmaker.domain.controllers.DomainController
import com.example.matchmaker.domain.classes.Tournament
import com.example.matchmaker.utils.isDarkModeEnabled

class ClassificationActivity : AppCompatActivity() {
    private lateinit var tournament: Tournament

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classification)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

        tournament = DomainController.getInstance().getTournament() as Tournament
        @Suppress("UNCHECKED_CAST")

        backButton.setOnClickListener {
            finish()
        }

         // Detect dark mode
        val isDarkMode = isDarkModeEnabled(this)

        // Get theme-aware colors
        val headerBackgroundColor = ContextCompat.getColor(this, android.R.color.darker_gray)
        val headerTextColor = ContextCompat.getColor(this, android.R.color.white)
        val cellBackgroundColor = if (isDarkMode) {
            "#1F1F1F".toColorInt()  // Dark gray for dark mode
        } else {
            Color.WHITE
        }
        val cellTextColor = if (isDarkMode) Color.WHITE else Color.BLACK

        val headers = arrayOf("", "Participante", "PJ", "PG", "PP", "SF", "SC", "Pts")
        val data : Array<Array<String>> = tournament.computeClassificationData()

        val headerRow = TableRow(this)
        headerRow.setBackgroundColor(headerBackgroundColor)

        for (header in headers) {
            val headerCell = TextView(this)
            headerCell.text = header
            headerCell.setPadding(8, 8, 8, 8)
            headerCell.textAlignment = View.TEXT_ALIGNMENT_CENTER
            headerCell.setBackgroundColor(headerBackgroundColor)
            headerCell.setTextColor(headerTextColor)
            headerCell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
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

    override fun onDestroy() {
        super.onDestroy()
        DomainController.getInstance().saveTournament(this, false)
    }
}