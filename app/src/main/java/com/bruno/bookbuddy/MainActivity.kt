package com.bruno.bookbuddy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.bruno.bookbuddy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()

        // Odgodi navigation setup dok se layout ne završi
        binding.root.post {
            initNavigation()
        }
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
    }

    private fun initNavigation() {
        try {
            // Sigurniji način dohvaćanja NavController-a
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.navController) as NavHostFragment
            val navController = navHostFragment.navController

            NavigationUI.setupWithNavController(binding.navView, navController)
        } catch (e: Exception) {
            // Fallback - bez navigation drawer-a za sada
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return try {
            toggleDrawer()
            true
        } catch (e: Exception) {
            super.onSupportNavigateUp()
        }
    }

    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawers()
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }
}