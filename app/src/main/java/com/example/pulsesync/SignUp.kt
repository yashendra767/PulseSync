package com.example.pulsesync

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.pulsesync.doctor.DoctorDashboard
import com.example.pulsesync.nurse.NurseDashboard
import com.example.pulsesync.pharmacist.PharmacistDashboard
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class SignUp : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var signUpEmail: TextInputEditText
    private lateinit var signUpPass: TextInputEditText
    private lateinit var signUpName: TextInputEditText
    private lateinit var roleSpinner: Spinner
    private lateinit var signUpButton: MaterialCardView
    private lateinit var tVLogin: TextView
    private lateinit var tVLogin2: TextView

    private val roles = arrayOf("Select Role", "Admin", "Doctor", "Nurse", "Pharmacist")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        redirectIfLoggedIn()

        setContentView(R.layout.activity_sign_up)

        signUpEmail = findViewById(R.id.signUpEmail)
        signUpPass = findViewById(R.id.signUpPass)
        signUpName = findViewById(R.id.signUpName)
        roleSpinner = findViewById(R.id.roleSpinner)
        signUpButton = findViewById(R.id.signUpButton)
        tVLogin = findViewById(R.id.tVLogin)
        tVLogin2 = findViewById(R.id.tVLogin2)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = adapter

        tVLogin.setOnClickListener {
            startActivity(Intent(this, login::class.java))
            finish()
        }

        tVLogin2.setOnClickListener {
            startActivity(Intent(this, login::class.java))
            finish()
        }

        signUpButton.setOnClickListener {
            val email = signUpEmail.text.toString().trim()
            val password = signUpPass.text.toString().trim()
            val name = signUpName.text.toString().trim()
            val role = roleSpinner.selectedItem.toString()

            if (validateInputs(email, password, name, role)) {
                registerUser(email, password, name, role)
            }
        }
    }

    private fun validateInputs(email: String, password: String, name: String, role: String): Boolean {
        if (TextUtils.isEmpty(email)) {
            signUpEmail.error = "Email is required."
            return false
        }
        if (TextUtils.isEmpty(password) || password.length < 6) {
            signUpPass.error = "Password must be at least 6 characters."
            return false
        }
        if (TextUtils.isEmpty(name)) {
            signUpName.error = "Name is required."
            return false
        }
        if (role == "Select Role") {
            Toast.makeText(this, "Please select a valid role.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun registerUser(email: String, password: String, name: String, role: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val userMap = hashMapOf(
                            "userId" to userId,
                            "name" to name,
                            "email" to email,
                            "role" to role,
                            "createdAt" to System.currentTimeMillis()
                        )

                        db.collection("users").document(userId)
                            .set(userMap, SetOptions.merge())
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                                    when (role) {
                                        "Admin" -> startActivity(Intent(this, AdminDashboard::class.java))
                                        "Doctor" -> startActivity(Intent(this, DoctorDashboard::class.java))
                                        "Nurse" -> startActivity(Intent(this, NurseDashboard::class.java))
                                        "Pharmacist" -> startActivity(Intent(this, PharmacistDashboard::class.java))
                                    }
                                    finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error: ${e.message}")
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
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
