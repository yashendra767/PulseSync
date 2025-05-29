package com.example.pulsesync.pharmacist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.pulsesync.inventory.InventoryItem
import com.example.pulsesync.R
import com.google.firebase.firestore.FirebaseFirestore
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.journeyapps.barcodescanner.CaptureActivity

class PharmacistScanFragment : Fragment() {

    private lateinit var btnScanQR: CardView
    private lateinit var tvScanResult: TextView
    private lateinit var tvItemDetails: TextView

    private val db = FirebaseFirestore.getInstance()

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if(result.contents == null) {
            Toast.makeText(requireContext(), "Scan cancelled", Toast.LENGTH_SHORT).show()
            tvScanResult.text = "Scan cancelled"
            tvItemDetails.text = ""
        } else {
            tvScanResult.text = "Scanned code: ${result.contents}"
            fetchInventoryItemByUid(result.contents)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pharmacist_scan, container, false)
        btnScanQR = view.findViewById(R.id.btnScanQR)
        tvScanResult = view.findViewById(R.id.tvScanResult)
        tvItemDetails = view.findViewById(R.id.tvItemDetails)

        btnScanQR.setOnClickListener {
            startQRScanner()
        }

        return view
    }

    private fun startQRScanner() {
        val options = ScanOptions().apply {
            setPrompt("Scan the inventory item QR code")
            setBeepEnabled(true)
            setOrientationLocked(true)
        }
        barcodeLauncher.launch(options)
    }

    private fun fetchInventoryItemByUid(uid: String) {
        db.collection("inventory").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val item = document.toObject(InventoryItem::class.java)
                    if (item != null) {
                        displayItemDetails(item)
                    } else {
                        tvItemDetails.text = "Item data is corrupt."
                    }
                } else {
                    tvItemDetails.text = "No inventory item found with UID: $uid"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error fetching item: ${e.message}", Toast.LENGTH_LONG).show()
                tvItemDetails.text = ""
            }
    }

    private fun displayItemDetails(item: InventoryItem) {
        tvItemDetails.text = """
            Name: ${item.itemName}
            Category: ${item.category}
            Quantity: ${item.quantity}
            Expiry Date: ${item.expiryDate}
        """.trimIndent()
    }
}
