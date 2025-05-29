package com.example.pulsesync.doctor

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.example.pulsesync.Home
import com.example.pulsesync.Profile
import com.example.pulsesync.R
import com.example.pulsesync.Settings
import com.example.pulsesync.beds.BedsAdmissions
import com.example.pulsesync.reports.ReportsAnalytics
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DoctorDashboard : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_dashboard)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        drawerLayout = findViewById(R.id.drawerLayoutDoctor)
        navigationView = findViewById(R.id.navigationViewDoctor)

        val headerView = navigationView.getHeaderView(0)
        val nameTextView = headerView.findViewById<TextView>(R.id.doctorName)
        val emailTextView = headerView.findViewById<TextView>(R.id.doctorEmail)
        val roleTextView = headerView.findViewById<TextView>(R.id.doctorRole)

        ensureDoctorProfileExists()

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        nameTextView.text = document.getString("name") ?: "Doctor"
                        emailTextView.text = currentUser.email ?: "doctor@example.com"
                        roleTextView.text = document.getString("role") ?: "Doctor"
                    }
                }
                .addOnFailureListener {
                    nameTextView.text = "Doctor Name"
                    emailTextView.text = currentUser.email ?: "doctor@example.com"
                    roleTextView.text = "Doctor"
                }
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationViewDoctor)

        val toolbar = findViewById<Toolbar>(R.id.toolbarDoctor)
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
            .replace(R.id.fragmentContainerDoctor, DoctorHome())
            .commit()

        bottomNavigationView.itemIconTintList = null

        bottomNavigationView.setOnItemSelectedListener { item ->
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerDoctor)
            when (item.itemId) {
                R.id.nav_home -> {
                    if (currentFragment !is DoctorHome) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainerDoctor, DoctorHome())
                            .commit()
                    }
                    true
                }
                R.id.nav_tasks -> {
                    if (currentFragment !is DoctorTasks) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainerDoctor, DoctorTasks())
                            .commit()
                    }
                    true
                }
                R.id.nav_profile -> {
                    if (currentFragment !is Profile) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainerDoctor, Profile())
                            .commit()
                    }
                    true
                }
                else -> false
            }
        }

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.drawer_admissions -> startActivity(Intent(this, BedsAdmissions::class.java))
                R.id.drawer_reports -> startActivity(Intent(this, ReportsAnalytics::class.java))
                R.id.drawer_profile -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerDoctor, Profile()).commit()
                R.id.drawer_settings -> startActivity(Intent(this, Settings::class.java))
                R.id.drawer_help -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.sih.gov.in/sih2020")))
            }
            drawerLayout.closeDrawers()
            true
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveTaskToBack(true)
            }
        })
    }
    private fun ensureDoctorProfileExists() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

        val doctorRef = firestore.collection("doctors").document(uid)

        doctorRef.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                firestore.collection("users").document(uid).get()
                    .addOnSuccessListener { userDoc ->
                        if (userDoc.exists()) {
                            val name = userDoc.getString("name") ?: "Doctor"
                            val email = userDoc.getString("email") ?: "unknown@example.com"
                            val doctorData = hashMapOf(
                                "name" to name,
                                "email" to email,
                                "assignedBeds" to listOf<String>()
                            )
                            doctorRef.set(doctorData).addOnSuccessListener {
                                println("Doctor profile created successfully.")
                            }.addOnFailureListener {
                                println("Failed to create doctor profile: ${it.message}")
                            }
                        }
                    }
            }
        }
    }

}