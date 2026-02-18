package com.example.matchmaker.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.example.matchmaker.R
import com.example.matchmaker.domain.controllers.DomainController
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize DomainController with application context
        DomainController.getInstance().initialize(applicationContext)

        val drawerLayout = findViewById<DrawerLayout>(R.id.mainDrawer)
        val navView = findViewById<NavigationView>(R.id.mainNavView)
        val menuButton = findViewById<ImageButton>(R.id.menuButton)
        val importButton = findViewById<ImageButton>(R.id.importButton)
        val createButton = findViewById<ImageButton>(R.id.createTournamentButton)
        val loadButton = findViewById<ImageButton>(R.id.loadTournamentButton)

        val importTournamentLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri == null) {
                return@registerForActivityResult
            }

            lifecycleScope.launch {
                val tournament = DomainController.getInstance().importTournament(this@MainActivity, uri)
                if (tournament == null) {
                    Toast.makeText(this@MainActivity, "Error al importar el torneo", Toast.LENGTH_LONG).show()
                    return@launch
                }

                DomainController.getInstance().setTournament(tournament)
                Toast.makeText(this@MainActivity, "Torneo importado correctamente", Toast.LENGTH_LONG).show()
                startActivity(Intent(this@MainActivity, TournamentActivity::class.java))
            }
        }

        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        importButton.setOnClickListener {
            importTournamentLauncher.launch(arrayOf("application/json"))
        }

        navView.setNavigationItemSelectedListener { item ->
            item.isChecked = true
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        createButton.setOnClickListener {
            val intent = Intent(this, CreateTournamentActivity::class.java)
            startActivity(intent)
        }

        loadButton.setOnClickListener {
            val intent = Intent(this, LoadTournamentActivity::class.java)
            startActivity(intent)
        }
    }
}