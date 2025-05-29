package com.example.pulsesync

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.os.Handler
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.os.postDelayed
import androidx.core.view.ViewCompat
import com.example.pulsesync.login
import androidx.core.view.WindowInsetsCompat


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val moveButton = findViewById<CardView>(R.id.moveButton)

        moveButton.visibility = View.GONE

        Handler(Looper.getMainLooper()).postDelayed({
            progressBar.visibility = View.GONE
            moveButton.visibility = View.VISIBLE
        }, 3000)

        moveButton.setOnClickListener{
            startActivity(Intent(this, login::class.java))
        }
    }
}