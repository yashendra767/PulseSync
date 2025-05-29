package com.example.pulsesync.beds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.pulsesync.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore

class AddBedBottomSheet(private val listener: OnBedAddedListener) : BottomSheetDialogFragment() {

    private lateinit var bedIdEditText: EditText
    private lateinit var bedTypeEditText: EditText
    private lateinit var bedStatusEditText: EditText
    private lateinit var patientNameEditText: EditText
    private val firestore = FirebaseFirestore.getInstance()

    interface OnBedAddedListener {
        fun onBedAdded()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_add_bed, container, false)

        bedIdEditText = view.findViewById(R.id.etBedId)
        bedTypeEditText = view.findViewById(R.id.etBedType)
        bedStatusEditText = view.findViewById(R.id.etBedStatus)
        patientNameEditText = view.findViewById(R.id.etPatientName)
        val btnSave: Button = view.findViewById(R.id.btnSaveBed)

        btnSave.setOnClickListener {
            val bedId = bedIdEditText.text.toString().trim()
            val type = bedTypeEditText.text.toString().trim()
            val status = bedStatusEditText.text.toString().trim()
            val patientName = patientNameEditText.text.toString().trim()

            if (bedId.isEmpty() || type.isEmpty() || status.isEmpty()) {
                Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bed = BedItem(bedId, type, status, patientName)
            firestore.collection("beds").document(bedId)
                .set(bed)
                .addOnSuccessListener {
                    Toast.makeText(context, "Bed added", Toast.LENGTH_SHORT).show()
                    listener.onBedAdded()
                    dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to add bed", Toast.LENGTH_SHORT).show()
                }
        }
        return view
    }
}
