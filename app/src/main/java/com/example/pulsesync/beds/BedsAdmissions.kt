package com.example.pulsesync.beds

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.pulsesync.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class BedsAdmissions : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var fabAdd: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beds_admissions)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        toolbar = findViewById(R.id.toolbar)
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.bedsTabs)
        fabAdd = findViewById(R.id.fabAdd)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val adapter = BedsAdmissionPagerAdapter(this)
        viewPager.adapter = adapter

        val tabTitles = listOf("Beds", "Admissions")
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        fabAdd.setOnClickListener {
            when (viewPager.currentItem) {
                0 -> {
                    val addBedSheet = AddBedBottomSheet(object : AddBedBottomSheet.OnBedAddedListener {
                        override fun onBedAdded() {
                            val bedsFragment = supportFragmentManager.findFragmentByTag("f0") as? Beds
                            bedsFragment?.fetchBeds()
                        }
                    })
                    addBedSheet.show(supportFragmentManager, "AddBedBottomSheet")
                }
                1 -> {
                    val addAdmissionSheet = AddAdmissionBottomSheet(object : AddAdmissionBottomSheet.OnAdmissionAddedListener {
                        override fun onAdmissionAdded() {
                            val admissionFragment = supportFragmentManager.findFragmentByTag("f1") as? Admission
                            admissionFragment?.fetchAdmissions()
                        }
                    })
                    addAdmissionSheet.show(supportFragmentManager, "AddAdmissionBottomSheet")
                }
            }
        }
    }
}
