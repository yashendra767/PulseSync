package com.example.pulsesync

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        redirectIfLoggedIn()

        setContentView(R.layout.activity_login)

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
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
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
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to check user session", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
