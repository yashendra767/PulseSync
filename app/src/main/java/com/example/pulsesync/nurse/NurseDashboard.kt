package com.example.pulsesync.nurse

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.pulsesync.Profile
import com.example.pulsesync.R
import com.example.pulsesync.Settings
import com.example.pulsesync.beds.BedsAdmissions
import com.example.pulsesync.inventory.InventoryManagement
import com.example.pulsesync.reports.ReportsAnalytics
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class NurseDashboard : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var bottomNav: BottomNavigationView
    private var currentSelectedItemId: Int = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nurse_dashboard)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        drawerLayout = findViewById(R.id.nurse_drawer_layout)
        navView = findViewById(R.id.nurse_nav_view)
        bottomNav = findViewById(R.id.nurse_bottom_nav)

        val toolbar: Toolbar = findViewById(R.id.nurse_toolbar)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportFragmentManager.beginTransaction()
            .replace(R.id.nurse_fragment_container, NurseHomeFragment())
            .commit()

        bottomNav.itemIconTintList =null

        bottomNav.setOnItemSelectedListener {
            if (it.itemId == currentSelectedItemId) {
                return@setOnItemSelectedListener false
            }

            currentSelectedItemId = it.itemId

            when (it.itemId) {
                R.id.nav_home -> switchFragment(NurseHomeFragment())
                R.id.nav_tasks -> switchFragment(NurseTasksFragment())
                R.id.nav_profile -> switchFragment(Profile())
            }
            true
        }

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_bed_info -> startActivity(Intent(this, BedsAdmissions::class.java))
                R.id.menu_inventory -> startActivity(Intent(this, InventoryManagement::class.java))
                R.id.menu_settings -> startActivity(Intent(this, Settings::class.java))
                R.id.menu_reports -> startActivity(Intent(this, ReportsAnalytics::class.java))
                R.id.menu_help -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.sih.gov.in/sih2020")))
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveTaskToBack(true)
            }
        })
    }

    fun switchFragment(fragment: Fragment) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.nurse_fragment_container)
        if (currentFragment != null && currentFragment::class == fragment::class) {
            return
        }
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nurse_fragment_container, fragment)
        fragmentTransaction.commit()
    }
}