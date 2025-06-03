package com.example.pulsesync

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.pulsesync.doctor.DoctorDashboard
import com.example.pulsesync.nurse.NurseDashboard
import com.example.pulsesync.pharmacist.PharmacistDashboard
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var loginEmail: TextInputEditText
    private lateinit var loginPass: TextInputEditText
    private lateinit var loginButton: MaterialCardView
    private lateinit var tVSignUp: TextView
    private lateinit var tVSignUp2: TextView
    private lateinit var forgotPass: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("PulseSyncPrefs", Context.MODE_PRIVATE)
        setContentView(R.layout.activity_login)

        redirectIfLoggedIn()

        loginEmail = findViewById(R.id.loginEmail)
        loginPass = findViewById(R.id.loginPass)
        loginButton = findViewById(R.id.loginButton)
        tVSignUp = findViewById(R.id.tVSignUp)
        tVSignUp2 = findViewById(R.id.tVSignUp2)
        forgotPass = findViewById(R.id.tVForgotPass)


        tVSignUp.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
            finish()
        }

        tVSignUp2.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
            finish()
        }

        forgotPass.setOnClickListener {
            startActivity(Intent(this, ForgotPass::class.java))
            finish()
        }

        loginButton.setOnClickListener {
            val email = loginEmail.text.toString().trim()
            val password = loginPass.text.toString().trim()

            if (validateInputs(email, password)) {
                loginUser(email, password)
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (TextUtils.isEmpty(email)) {
            loginEmail.error = "Email is required."
            return false
        }
        if (TextUtils.isEmpty(password)) {
            loginPass.error = "Password is required."
            return false
        }
        return true
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.uid?.let { fetchUserRole(it) }
                } else {
                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun fetchUserRole(userId: String) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    when (role) {
                            "Admin" -> startActivity(Intent(this, AdminDashboard::class.java))
                            "Doctor" -> startActivity(Intent(this, DoctorDashboard::class.java))
                            "Nurse" -> startActivity(Intent(this, NurseDashboard::class.java))
                            "Pharmacist" -> startActivity(Intent(this, PharmacistDashboard::class.java))
                        }
                        finish()
                } else {
                    Toast.makeText(this, "User record not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun redirectIfLoggedIn() {
        val currentUser = auth.currentUser
        val biometricEnabled = sharedPreferences.getBoolean("biometric_enabled", false)

        if (currentUser != null && biometricEnabled) {
            showBiometricPrompt()
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)

        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)

                Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(applicationContext, "Authentication succeeded", Toast.LENGTH_SHORT).show()
                auth.currentUser?.uid?.let { fetchUserRole(it) }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(applicationContext, "Biometric not recognized. Try again.", Toast.LENGTH_SHORT).show()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Use your fingerprint or screen lock to continue")
            .setAllowedAuthenticators(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }


}
