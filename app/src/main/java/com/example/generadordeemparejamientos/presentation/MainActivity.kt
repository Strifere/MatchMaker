package com.example.generadordeemparejamientos.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.generadordeemparejamientos.R
import com.example.generadordeemparejamientos.domain.controllers.DomainController
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize DomainController with application context
        DomainController.getInstance().initialize(applicationContext)

        val drawerLayout = findViewById<DrawerLayout>(R.id.mainDrawer)
        val navView = findViewById<NavigationView>(R.id.mainNavView)
        val menuButton = findViewById<ImageButton>(R.id.menuButton)
        val createButton = findViewById<ImageButton>(R.id.createTournamentButton)
        val loadButton = findViewById<ImageButton>(R.id.loadTournamentButton)

        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
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