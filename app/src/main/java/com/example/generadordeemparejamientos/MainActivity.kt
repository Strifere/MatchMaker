package com.example.generadordeemparejamientos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.CheckResult
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val input = findViewById<TextInputEditText>(R.id.inputBox)
        val sets = findViewById<TextInputEditText>(R.id.setsBox)
        val submitButton = findViewById<Button>(R.id.submitButton)
        val resultText = findViewById<TextView>(R.id.testo)
        val includeSetsResults = findViewById<CheckBox>(R.id.includeSetsResults)

        submitButton.setOnClickListener {
            val numJugadores = input.text.toString().trim().toIntOrNull()
            val numSets = sets.text.toString().trim().toIntOrNull()
            if (numJugadores == null || numJugadores < 2) {
                resultText.text = "El número de jugadores debe ser al menos 2."
                return@setOnClickListener
            }
            if (numSets == null) {
                resultText.text = "Introduzca el número de sets máximo por partido."
                return@setOnClickListener
            } else if (numSets%2 == 0) {
                resultText.text = "El número de sets debe ser impar."
                return@setOnClickListener
            }

            // Navigate to NamesActivity
            val intent = Intent(this, NamesActivity::class.java)
            intent.putExtra("numJugadores", numJugadores)
            intent.putExtra("numSets", numSets)
            intent.putExtra("includeSetsResults", includeSetsResults.isChecked)
            startActivity(intent)
        }
    }
}