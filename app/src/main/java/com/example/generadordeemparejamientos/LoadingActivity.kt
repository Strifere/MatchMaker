package com.example.generadordeemparejamientos

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoadingActivity : AppCompatActivity() {
    private var startTime = 0L
    private var currentRound = 0
    private var totalRounds = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        val numJugadores = intent.getIntExtra("numJugadores", 0)
        val nombres = intent.getStringArrayExtra("nombres") ?: arrayOf()
        val numSets = intent.getIntExtra("numSets", 1)
        val includeSetsResults = intent.getBooleanExtra("includeSetsResults", false)

        val timerText = findViewById<TextView>(R.id.timerText)
        val linearProgressBar = findViewById<ProgressBar>(R.id.linearProgressBar)

        totalRounds = if (numJugadores % 2 == 1) numJugadores else numJugadores - 1
        linearProgressBar.max = totalRounds

        startTime = System.currentTimeMillis()

        // Generate the pairing table on a background thread to avoid blocking UI
        Thread {
            val tabla = generarTablaEmparejamientos(numJugadores)

            // Update UI on main thread when done
            runOnUiThread {
                val intent = Intent(this, RoundsActivity::class.java)
                val tournament = Tournament(
                    numJugadores = numJugadores,
                    nombres = nombres,
                    tabla = tabla.map { it.toIntArray() }.toTypedArray(),
                    bestOf = numSets,
                    includeSetResults = includeSetsResults
                )
                // Store the tournament in the singleton
                TournamentApplication.setTournament(tournament)
                startActivity(intent)
                finish()
            }
        }.start()

        // Update timer every 100ms
        val timerHandler = Handler(Looper.getMainLooper())
        timerHandler.post(object : Runnable {
            override fun run() {
                val elapsedTime = (System.currentTimeMillis() - startTime) / 1000
                timerText.text = "${elapsedTime}s"
                timerHandler.postDelayed(this, 100)
            }
        })
    }

    private fun updateProgress(operation: String) {
        runOnUiThread {
            findViewById<TextView>(R.id.currentOperationText).text = operation
            findViewById<ProgressBar>(R.id.linearProgressBar).progress = currentRound
        }
    }

    private fun shiftNumbers(cruces: MutableList<Int>, N: Int) {
        var aux = cruces[N-1]
        for (i in 1 until N) {
            val temp = cruces[i]
            cruces[i] = aux
            aux = temp
        }
    }

    fun generarTablaEmparejamientos(numJugadores: Int): List<List<Int>> {
        val impar = numJugadores % 2 == 1
        val N = if (impar) numJugadores + 1 else numJugadores
        val nRondas = N - 1
        val maxEmparejamientosPorRonda = N / 2
        var ronda = 0
        updateProgress("Generando tabla de emparejamientos...")
        val tablaTemporal = MutableList(N) { fila ->
            MutableList(N) { columna -> if (fila >= columna) -2 else -1 }
        }
        val cruces = (0 until N).toMutableList()
        updateProgress("Calculando emparejamientos...")
        while (ronda < nRondas) {
            var contador = 0
            while (contador < maxEmparejamientosPorRonda) {
                val jugador1 = cruces[contador]
                val jugador2 = cruces[N - 1 - contador]
                if (jugador1 < jugador2) tablaTemporal[jugador1][jugador2] = ronda
                else if (jugador1 > jugador2) tablaTemporal[jugador2][jugador1] = ronda
                contador++
            }
            shiftNumbers(cruces, N);
            ++ronda
        }

        if (impar) {
            updateProgress("Recalculando tabla sin 'bye'...")
            // Remove the "bye" player from the table
            val tablaSinBye = MutableList(numJugadores) {
                MutableList(numJugadores) { -2 }
            }
            for (i in 0 until numJugadores) {
                for (j in 0 until numJugadores) {
                    tablaSinBye[i][j] = tablaTemporal[i][j]
                }
            }
            updateProgress("Generando visualización...")
            return tablaSinBye
        }
        else return tablaTemporal
    }
}

