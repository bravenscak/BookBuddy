package com.bruno.bookbuddy

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.bruno.bookbuddy.databinding.ActivityMainBinding
import com.bruno.bookbuddy.utils.LocaleHelper
import com.bruno.bookbuddy.utils.SampleDataHelper
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentLanguage = LocaleHelper.getCurrentLanguage(this)
        if (currentLanguage != Locale.getDefault().language) {
            LocaleHelper.setLocale(this, currentLanguage)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkNotificationPermission()
        initToolbar()
        initSampleData()

        binding.root.post {
            initNavigation()
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun initSampleData() {
        SampleDataHelper.addSampleBooksIfEmpty(this)
    }

    private fun initToolbar() {
        if (supportActionBar == null) {
            setSupportActionBar(binding.toolbar)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
    }

    private fun initNavigation() {
        try {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.navController) as NavHostFragment
            val navController = navHostFragment.navController

            NavigationUI.setupWithNavController(binding.navView, navController)
        } catch (e: Exception) {
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

    override fun attachBaseContext(newBase: Context) {
        val language = LocaleHelper.getCurrentLanguage(newBase)
        val context = LocaleHelper.setLocale(newBase, language)
        super.attachBaseContext(context)
    }

    override fun onResume() {
        super.onResume()
        updateLastOpenTime()
    }

    private fun updateLastOpenTime() {
        val prefs = getSharedPreferences("book_buddy_prefs", MODE_PRIVATE)
        prefs.edit()
            .putLong("last_open_time", System.currentTimeMillis())
            .apply()
    }
}