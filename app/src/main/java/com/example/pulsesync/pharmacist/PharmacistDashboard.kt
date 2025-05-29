package com.example.pulsesync.pharmacist

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.pulsesync.Profile
import com.example.pulsesync.R
import com.example.pulsesync.Settings
import com.example.pulsesync.databinding.ActivityPharmacistDashboardBinding
import com.example.pulsesync.inventory.InventoryManagement
import com.google.android.material.navigation.NavigationView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PharmacistDashboard : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityPharmacistDashboardBinding
    private var currentSelectedItemId: Int = R.id.nav_home


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPharmacistDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        setSupportActionBar(binding.pharmacistToolbar)

        val headerView = binding.pharmacistNavView.getHeaderView(0)
        val pharmacistName = headerView.findViewById<TextView>(R.id.pharmacistName)
        val pharmacistEmail = headerView.findViewById<TextView>(R.id.pharmacistEmail)
        val pharmacistRole = headerView.findViewById<TextView>(R.id.pharmacistRole)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        pharmacistName.text = document.getString("name") ?: "Pharmacist Name"
                        pharmacistEmail.text = currentUser.email ?: "pharmacist@example.com"
                        pharmacistRole.text = document.getString("role") ?: "Pharmacist"
                    }
                }
                .addOnFailureListener {
                    pharmacistName.text = "Pharmacist Name"
                    pharmacistEmail.text = currentUser.email ?: "pharmacist@example.com"
                    pharmacistRole.text = "Pharmacist"
                }
        }

        val toggle = ActionBarDrawerToggle(
            this,
            binding.pharmacistDrawerLayout,
            binding.pharmacistToolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        binding.pharmacistDrawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.pharmacistNavView.setNavigationItemSelectedListener(this)

        binding.pharmacistBottomNav.itemIconTintList = null

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.pharmacist_fragment_container, PharmacistHomeFragment())
                .commit()
            binding.pharmacistBottomNav.selectedItemId = R.id.nav_home
        }

        binding.pharmacistBottomNav.setOnItemSelectedListener(bottomNavListener)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveTaskToBack(true)
            }
        })
    }

    private val bottomNavListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        if (item.itemId == currentSelectedItemId) {
            return@OnNavigationItemSelectedListener false
        }

        currentSelectedItemId = item.itemId

        val fragment = when (item.itemId) {
            R.id.nav_home -> PharmacistHomeFragment()
            R.id.nav_scan -> PharmacistScanFragment()
            R.id.nav_profile -> Profile()
            else -> null
        }

        fragment?.let {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.pharmacist_fragment_container)
            if (currentFragment != null && currentFragment::class == fragment::class) {
                return@OnNavigationItemSelectedListener false
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.pharmacist_fragment_container, it)
                .commit()
            true
        } ?: false
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_inventory -> {
                startActivity(Intent(this, InventoryManagement::class.java))
            }
            R.id.nav_dispense_history -> {
                startActivity(Intent(this, DispensingHistory::class.java))
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, Settings::class.java))
            }
            R.id.nav_help -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.sih.gov.in/sih2020")))
            }
        }
        binding.pharmacistDrawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (binding.pharmacistDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.pharmacistDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
