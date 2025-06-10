package com.example.pulsesync

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.pulsesync.beds.BedsAdmissions
import com.example.pulsesync.inventory.InventoryManagement
import com.example.pulsesync.reports.ReportsAnalytics
import com.example.pulsesync.user_manage.UsersManagement
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminDashboard : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var bottomNav: BottomNavigationView
    private var currentSelectedItemId: Int = R.id.nav_home


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_admin_dashboard)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.adminNavView)

        val headerView = navigationView.getHeaderView(0)
        val nameTextView = headerView.findViewById<TextView>(R.id.adminName)
        val emailTextView = headerView.findViewById<TextView>(R.id.adminEmail)
        val roleTextView = headerView.findViewById<TextView>(R.id.adminRole)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        nameTextView.text = document.getString("name") ?: "Admin"
                        emailTextView.text = currentUser.email ?: "admin@example.com"
                        roleTextView.text = document.getString("role") ?: "Admin"
                    }
                }
                .addOnFailureListener {
                    nameTextView.text = "Admin"
                    emailTextView.text = currentUser?.email ?: "admin@example.com"
                    roleTextView.text = "Admin"
                }
        }

        bottomNav=findViewById(R.id.adminBottomNav)
        bottomNav.itemIconTintList=null

        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        replaceTheFragment(Home())
        bottomNav.setOnItemSelectedListener {
            if (it.itemId == currentSelectedItemId) {
                return@setOnItemSelectedListener false
            }

            currentSelectedItemId = it.itemId

            when (it.itemId) {
                R.id.nav_home -> replaceTheFragment(Home())
                R.id.nav_tasks -> replaceTheFragment(AdminTasks())
                R.id.nav_profile -> replaceTheFragment(Profile())
            }
            true
        }


        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_users -> {
                    startActivity(Intent(this, UsersManagement::class.java))
                }
                R.id.nav_inventory -> {
                    startActivity(Intent(this, InventoryManagement::class.java))
                }
                R.id.nav_beds->{
                    startActivity(Intent(this, BedsAdmissions::class.java))
                }
                R.id.nav_reports ->{
                    startActivity(Intent(this, ReportsAnalytics::class.java))
                }
                R.id.nav_settings ->{
                    startActivity(Intent(this, Settings::class.java))
                }
                R.id.nav_help -> {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.sih.gov.in/sih2020")))
                }
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

    fun replaceTheFragment(fragment: Fragment) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.adminFragmentContainer)
        if (currentFragment != null && currentFragment::class == fragment::class) {
            return
        }
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.adminFragmentContainer, fragment)
        fragmentTransaction.commit()
    }

}


