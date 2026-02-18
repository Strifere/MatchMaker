package com.example.matchmaker.presentation

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.matchmaker.R
import com.example.matchmaker.domain.classes.Tournament
import com.example.matchmaker.domain.controllers.DomainController

class PlayersActivity : AppCompatActivity() {
    private lateinit var playersContainer: LinearLayout
    private lateinit var tournament: Tournament
    private lateinit var searchPlayerBar: SearchView
    private lateinit var playersTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_players)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        playersContainer = findViewById(R.id.playersContainer)
        searchPlayerBar = findViewById(R.id.searchButton)
        playersTitle = findViewById(R.id.playersTitle)

        searchPlayerBar.setOnClickListener {
            playersTitle.visibility = View.GONE // Hide the title when searching
        }
        searchPlayerBar.setOnCloseListener {
            playersTitle.visibility = View.VISIBLE // Show the title when search is closed
            false
        }
        searchPlayerBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterPlayers(newText ?: "")
                return true
            }
        })

        tournament = DomainController.getInstance().getTournament() as Tournament

        backButton.setOnClickListener {
            finish()
        }

        renderPlayers()
    }

    override fun onResume() {
        super.onResume()
        renderPlayers()
    }

    override fun onDestroy() {
        super.onDestroy()
        DomainController.getInstance().saveTournament(this, false)
    }

    private fun renderPlayers() {
        tournament.updatePlayerStats() // Ensure player stats are up to date before rendering
        playersContainer.removeAllViews()
        for (player in tournament.players) {
            val playerView = layoutInflater.inflate(R.layout.player_card, playersContainer, false)
            val name = playerView.findViewById<TextView>(R.id.playerName)
            val pj = playerView.findViewById<TextView>(R.id.pjText)
            val pg = playerView.findViewById<TextView>(R.id.pgText)
            val pp = playerView.findViewById<TextView>(R.id.ppText)
            val sf = playerView.findViewById<TextView>(R.id.sfText)
            val sc = playerView.findViewById<TextView>(R.id.scText)
            val points = playerView.findViewById<TextView>(R.id.pointsText)

            val playerMatchesButton =
                playerView.findViewById<android.widget.Button>(R.id.playerMatchesButton)
            playerMatchesButton.setOnClickListener {
                val intent = android.content.Intent(this, RoundsActivity::class.java)
                intent.putExtra("playerName", player.name)
                startActivity(intent)
            }

            val editNameButton = playerView.findViewById<ImageButton>(R.id.editNameButton)
            val input = EditText(this).apply {
                inputType = InputType.TYPE_CLASS_TEXT
                hint = "Introduce aquí el nuevo nombre del jugador"
                setPadding(40,30,40,30)
            }
            editNameButton.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Cambiar nombre del jugador: ${player.name}")
                    .setView(input)
                    .setPositiveButton("Confirmar") { _, _ ->
                        val newName = input.text.toString().trim()
                        if (newName.isBlank()) {
                            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                        if (tournament.players.any { it.name.equals(newName, ignoreCase = true) }) {
                            Toast.makeText(this, "Ya existe un jugador con ese nombre", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                        player.name = newName
                        DomainController.getInstance().saveTournament(this, false)
                        renderPlayers() // Refresh the list to show the updated name
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

            name.text = player.name
            pj.text = player.pj.toString()
            pg.text = player.pg.toString()
            pp.text = player.pp.toString()
            sf.text = player.sf.toString()
            sc.text = player.sc.toString()
            points.text = player.pts.toString()

            playersContainer.addView(playerView)
        }
    }

    private fun filterPlayers(query: String) {
        for (i in 0 until playersContainer.childCount) {
            val playerView = playersContainer.getChildAt(i)
            val nameTextView = playerView.findViewById<TextView>(R.id.playerName)
            val playerName = nameTextView.text.toString()
            playerView.visibility = if (playerName.contains(query, ignoreCase = true)) View.VISIBLE else View.GONE
        }
    }
}