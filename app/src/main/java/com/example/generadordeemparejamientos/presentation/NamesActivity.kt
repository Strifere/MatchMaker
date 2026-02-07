package com.example.generadordeemparejamientos.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.example.generadordeemparejamientos.R

class NamesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_names)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val generateButton = findViewById<Button>(R.id.generateButton)
        val generateEmptyButton = findViewById<Button>(R.id.generateNoNamesButton)
        val namesContainer = findViewById<LinearLayout>(R.id.namesContainer)

        val numJugadores = intent.getIntExtra("numJugadores", 0)
        val numSets = intent.getIntExtra("numSets", 1)
        val includeSetsResults = intent.getBooleanExtra("includeSetsResults", false)

        // Create name input fields
        repeat(numJugadores) { index ->
            val editText = EditText(this)
            editText.hint = "Participante ${index + 1}"
            editText.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            editText.setPadding(16)
            namesContainer.addView(editText)
        }

        backButton.setOnClickListener {
            finish()
        }

        generateButton.setOnClickListener {
            val nombres = (0 until namesContainer.childCount).map { i ->
                val et = namesContainer.getChildAt(i) as EditText
                et.text.toString().trim()
            }

            if (nombres.any { it.isBlank() }) {
                // Show error - could add a Toast or TextView message here
                return@setOnClickListener
            }

            // Navigate to RoundsActivity
            val intent = Intent(this, LoadingActivity::class.java)
            intent.putExtra("numJugadores", numJugadores)
            intent.putExtra("nombres", nombres.toTypedArray())
            intent.putExtra("numSets", intent.getIntExtra("numSets", 1))
            startActivity(intent)
        }

        generateEmptyButton.setOnClickListener {
            val nombres = (1..namesContainer.childCount).map { i -> i.toString() }

            // Navigate to RoundsActivity
            val intent = Intent(this, LoadingActivity::class.java)
            intent.putExtra("numJugadores", numJugadores)
            intent.putExtra("nombres", nombres.toTypedArray())
            intent.putExtra("numSets", numSets)
            intent.putExtra("includeSetsResults", includeSetsResults)
            startActivity(intent)
        }
    }


}
