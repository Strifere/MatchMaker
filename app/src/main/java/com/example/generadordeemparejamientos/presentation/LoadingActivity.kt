package com.example.generadordeemparejamientos.presentation

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.generadordeemparejamientos.R
import com.example.generadordeemparejamientos.domain.classes.Player
import com.example.generadordeemparejamientos.domain.controllers.DomainController
import com.example.generadordeemparejamientos.domain.classes.Tournament

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

        totalRounds = if (numJugadores % 2 == 1) numJugadores else numJugadores - 1

        startTime = System.currentTimeMillis()

        // Generate the pairing table on a background thread to avoid blocking UI
        Thread {
            val tabla = generarTablaEmparejamientos(numJugadores)

            // Update UI on main thread when done
            runOnUiThread {
                val intent = Intent(this, RoundsActivity::class.java)
                val players = createPlayersList(numJugadores, nombres)
                val tournament = Tournament(
                    players = players,
                    bestOf = numSets,
                    includeSetResults = includeSetsResults
                )
                tournament.initialize(tabla)
                // Store the tournament in the singleton
                DomainController.setTournament(tournament)
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

    private fun shiftNumbers(cruces: MutableList<Int>, n: Int) {
        var aux = cruces[n-1]
        for (i in 1 until n) {
            val temp = cruces[i]
            cruces[i] = aux
            aux = temp
        }
    }

    private fun generarTablaEmparejamientos(numJugadores: Int): List<List<Int>> {
        val impar = numJugadores % 2 == 1
        val n = if (impar) numJugadores + 1 else numJugadores
        val nRondas = n - 1
        val maxEmparejamientosPorRonda = n / 2
        var ronda = 0
        val tablaTemporal = MutableList(n) { fila ->
            MutableList(n) { columna -> if (fila >= columna) -2 else -1 }
        }
        val cruces = (0 until n).toMutableList()
        while (ronda < nRondas) {
            var contador = 0
            while (contador < maxEmparejamientosPorRonda) {
                val jugador1 = cruces[contador]
                val jugador2 = cruces[n - 1 - contador]
                if (jugador1 < jugador2) tablaTemporal[jugador1][jugador2] = ronda
                else if (jugador1 > jugador2) tablaTemporal[jugador2][jugador1] = ronda
                contador++
            }
            shiftNumbers(cruces, n);
            ++ronda
        }

        if (impar) {
            // Remove the "bye" player from the table
            val tablaSinBye = MutableList(numJugadores) {
                MutableList(numJugadores) { -2 }
            }
            for (i in 0 until numJugadores) {
                for (j in 0 until numJugadores) {
                    tablaSinBye[i][j] = tablaTemporal[i][j]
                }
            }
            return tablaSinBye
        }
        else return tablaTemporal
    }

    private fun createPlayersList(numJugadores: Int, nombres: Array<String>): Array<Player> {
        val players = Array(numJugadores) { Player(name = "") }
        for (i in 0 until numJugadores) {
            val name = nombres[i]
            val player = Player(name = name)
            players[i] = player
        }
        return players
    }
}

