package com.example.pulsesync

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth
import androidx.core.content.edit

class Settings : AppCompatActivity() {

    private lateinit var llProfileInfo: LinearLayout
    private lateinit var llChangePassword: LinearLayout
    private lateinit var llLogout: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var switchNotifications: MaterialSwitch
    private lateinit var spinnerTheme: Spinner
    private lateinit var spinnerLanguage: Spinner

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        sharedPreferences = getSharedPreferences("PulseSyncPrefs", Context.MODE_PRIVATE)

        llProfileInfo = findViewById(R.id.llProfileInfo)
        llChangePassword = findViewById(R.id.llChangePassword)
        llLogout = findViewById(R.id.llLogout)
        switchNotifications = findViewById(R.id.switchNotifications)
        spinnerTheme = findViewById(R.id.spinnerTheme)
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        val toolbarSettings = findViewById<Toolbar>(R.id.toolbarSettings)
        val switchBiometric = findViewById<MaterialSwitch>(R.id.switchBiometric)
        val biometricEnabled = sharedPreferences.getBoolean("biometric_enabled", false)
        switchBiometric.isChecked = biometricEnabled

        switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit { putBoolean("biometric_enabled", isChecked) }
            Toast.makeText(this, "Biometric ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }


        toolbarSettings.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        setupTheme()
        setupNotifications()
        setupLanguage()

        setListeners()
    }

    private fun setListeners() {
        llProfileInfo.setOnClickListener {
            findViewById<View>(R.id.fragmentContainerSettings).visibility = View.VISIBLE

            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainerSettings, Profile())
            transaction.addToBackStack(null)
            transaction.commit()

        }

        llChangePassword.setOnClickListener {
            changePassword()
        }

        llLogout.setOnClickListener {
            logoutUser()
        }

    }

    private fun setupNotifications() {
        val isNotificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true)
        switchNotifications.isChecked = isNotificationsEnabled

        var isFirstSwitch = true
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isFirstSwitch) {
                isFirstSwitch = false
                return@setOnCheckedChangeListener
            }

            sharedPreferences.edit { putBoolean("notifications_enabled", isChecked) }
            Toast.makeText(this, "Notifications ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setupTheme() {
        val themeOptions = resources.getStringArray(R.array.theme_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, themeOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTheme.adapter = adapter

        val savedTheme = sharedPreferences.getString("selected_theme", "System Default")
        spinnerTheme.setSelection(themeOptions.indexOf(savedTheme))

        var isFirstSelection = true
        spinnerTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }

                val selectedTheme = themeOptions[position]
                if (selectedTheme != savedTheme) {
                    sharedPreferences.edit().putString("selected_theme", selectedTheme).apply()

                    when (selectedTheme) {
                        "Light" -> setTheme(R.style.Theme_PulseSync_Light)
                        "Dark" -> setTheme(R.style.Theme_PulseSync_Dark)
                        else -> setTheme(R.style.Theme_PulseSync)
                    }
                    recreate()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }



    private fun setupLanguage() {
        val languageOptions = resources.getStringArray(R.array.language_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languageOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = adapter

        val savedLanguage = sharedPreferences.getString("selected_language", "English")
        spinnerLanguage.setSelection(languageOptions.indexOf(savedLanguage))

        var isFirstSelection = true
        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }

                val selectedLanguage = languageOptions[position]
                if (selectedLanguage != savedLanguage) {
                    sharedPreferences.edit { putString("selected_language", selectedLanguage) }
                    Toast.makeText(this@Settings, "Language set to $selectedLanguage", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    private fun changePassword() {
        auth.currentUser?.let { user ->
            auth.sendPasswordResetEmail(user.email!!)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to send reset email", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun logoutUser() {
        auth.signOut()
        finish()
    }

}
