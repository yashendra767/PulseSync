package com.example.pulsesync.beds

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.pulsesync.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.abs

class AddAdmissionBottomSheet(private val listener: OnAdmissionAddedListener, private val existingAdmission: AdmissionItem? = null) : BottomSheetDialogFragment() {

    private lateinit var patientNameEditText: EditText
    private lateinit var bedIdEditText: EditText
    private lateinit var admissionDateEditText: EditText
    private lateinit var admissionStatusEditText: EditText
    private val firestore = FirebaseFirestore.getInstance()

    interface OnAdmissionAddedListener {
        fun onAdmissionAdded()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_add_admission, container, false)
        val temperatureField = view.findViewById<TextInputEditText>(R.id.editTextTemperature)
        val oxygenField = view.findViewById<TextInputEditText>(R.id.editTextOxygen)
        val bpField = view.findViewById<TextInputEditText>(R.id.editTextBP)

        patientNameEditText = view.findViewById(R.id.etPatientNameAdm)
        bedIdEditText = view.findViewById(R.id.etBedIdAdm)
        admissionDateEditText = view.findViewById(R.id.etAdmissionDate)
        admissionStatusEditText = view.findViewById(R.id.etAdmissionStatus)
        val btnSave: Button = view.findViewById(R.id.btnSaveAdmission)

        existingAdmission?.let {
            patientNameEditText.setText(it.patientName)
            bedIdEditText.setText(it.bedId)
            admissionDateEditText.setText(it.date)
            admissionStatusEditText.setText(it.status)
            temperatureField.setText(it.temperature.toString())
            oxygenField.setText(it.oxygen.toString())
            bpField.setText(it.bloodPressure)
        }

        btnSave.setOnClickListener {
            val patientName = patientNameEditText.text.toString().trim()
            val bedId = bedIdEditText.text.toString().trim()
            val date = admissionDateEditText.text.toString().trim()
            val status = admissionStatusEditText.text.toString().trim()
            val temperature = temperatureField.text.toString().toDoubleOrNull() ?: 98.6
            val oxygen = oxygenField.text.toString().toDoubleOrNull() ?: 98.0
            val bloodPressure = bpField.text.toString().ifEmpty { "120/80" }

            if (patientName.isEmpty() || bedId.isEmpty() || date.isEmpty() || status.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val admissionMap = hashMapOf(
                "patientName" to patientName,
                "bedId" to bedId,
                "date" to date,
                "status" to status,
                "temperature" to temperature,
                "oxygen" to oxygen,
                "bloodPressure" to bloodPressure,
                "lastUpdated" to System.currentTimeMillis()
            )
            if (existingAdmission != null) {
                firestore.collection("admissions").document(existingAdmission.id)
                    .update(admissionMap as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Admission updated", Toast.LENGTH_SHORT).show()
                        listener.onAdmissionAdded()
                        dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show()
                    }
            } else {
                firestore.collection("admissions").add(admissionMap)
                    .addOnSuccessListener { documentRef ->
                        val docId = documentRef.id
                        documentRef.update("id", docId)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Admission added", Toast.LENGTH_SHORT).show()
                                assignBedToDoctorAutomatically(bedId)
                                listener.onAdmissionAdded()
                                dismiss()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to add admission", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        return view
}

    private fun assignBedToDoctorAutomatically(bedId: String) {
        val bedNumber = bedId.toIntOrNull() ?: return

        firestore.collection("doctors").get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val doctorId = doc.id
                    val assignedBeds = (doc.get("assignedBeds") as? List<String>)?.mapNotNull { it.toIntOrNull() } ?: emptyList()

                    val matches = assignedBeds.any { abs(it - bedNumber) <= 3 }

                    if (matches || assignedBeds.isEmpty()) {
                        firestore.collection("doctors")
                            .document(doctorId)
                            .update("assignedBeds", FieldValue.arrayUnion(bedId))
                            .addOnSuccessListener {
                                Log.d("DoctorAssign", "Bed $bedId assigned to doctor $doctorId")
                            }
                            .addOnFailureListener {
                                Log.e("DoctorAssign", "Failed to update doctor $doctorId", it)
                            }
                        break
                    }
                }
            }
            .addOnFailureListener {
                Log.e("DoctorAssign", "Failed to fetch doctors", it)
            }
    }
}
