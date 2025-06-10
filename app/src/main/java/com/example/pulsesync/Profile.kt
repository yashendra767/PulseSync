package com.example.pulsesync


import android.net.Uri
import android.os.Bundle
import okhttp3.Call
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.bumptech.glide.Glide
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import okhttp3.Callback

class Profile : Fragment() {

    private lateinit var ivProfilePicture: ImageView
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileEmail: TextView
    private lateinit var tvProfileRole: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var btnLogout: Button
    private lateinit var userId: String
    private lateinit var ivEditProfilePicture: ImageView
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            uploadImageToCloudinary(uri)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture)
        tvProfileName = view.findViewById(R.id.tvProfileName)
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail)
        tvProfileRole = view.findViewById(R.id.tvProfileRole)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnLogout = view.findViewById(R.id.btnLogout)
        userId = auth.currentUser?.uid ?: ""
        ivEditProfilePicture = view.findViewById(R.id.ivEditProfilePicture)

        loadUserProfile()

        btnEditProfile.setOnClickListener {
            openEditProfileBottomSheet()
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            requireActivity().finish()
        }
        ivEditProfilePicture.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

    }

    private fun openEditProfileBottomSheet() {
        val bottomSheetView = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_edit_profile, null)
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
        val etName = bottomSheetView.findViewById<EditText>(R.id.etEditName)
        val etEmail = bottomSheetView.findViewById<EditText>(R.id.etEditEmail)
        val etRole = bottomSheetView.findViewById<EditText>(R.id.etEditRole)
        val btnSave = bottomSheetView.findViewById<Button>(R.id.btnSaveProfile)

        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                etName.setText(document.getString("name") ?: "")
                etEmail.setText(document.getString("email") ?: "")
                etRole.setText(document.getString("role") ?: "")
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load profile data", Toast.LENGTH_SHORT)
                    .show()
            }

        btnSave.setOnClickListener {
            val updatedName = etName.text.toString().trim()
            val updatedEmail = etEmail.text.toString().trim()
            val updatedRole = etRole.text.toString().trim()

            if (updatedName.isEmpty() || updatedEmail.isEmpty() || updatedRole.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val currentUser = auth.currentUser

            if (currentUser == null) {
                Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            val updateData = mapOf(
                    "name" to updatedName,
                    "email" to updatedEmail,
                    "role" to updatedRole
            )

            currentUser.updateEmail(updatedEmail)
                .addOnSuccessListener {
                    firestore.collection("users").document(userId)
                        .update(updateData)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT)
                                .show()
                            tvProfileName.text = updatedName
                            tvProfileEmail.text = updatedEmail
                            tvProfileRole.text = "Role: $updatedRole"
                            bottomSheetDialog.dismiss()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                requireContext(),
                                "Failed to update Firestore: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                }
                .addOnFailureListener { e ->
                    if (e.message?.contains("recent login") == true) {
                        Toast.makeText(
                            requireContext(),
                            "Re-authentication required to update email.",
                            Toast.LENGTH_LONG
                        ).show()
                        reAuthenticateUser(updatedEmail, updateData)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to update email: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

        private fun reAuthenticateUser(updatedEmail: String, updateData: Map<String, Any>) {
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_reauthenticate, null)
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(bottomSheetView)

        val etPassword: EditText = bottomSheetView.findViewById(R.id.etPassword)
        val btnConfirm: Button = bottomSheetView.findViewById(R.id.btnConfirm)

        btnConfirm.setOnClickListener {
            val password = etPassword.text.toString().trim()

            if (password.isEmpty()) {
                Toast.makeText(requireContext(), "Password is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = auth.currentUser ?: return@setOnClickListener
            val credential = EmailAuthProvider.getCredential(user.email!!, password)

            user.reauthenticate(credential)
                .addOnSuccessListener {
                    user.updateEmail(updatedEmail)
                        .addOnSuccessListener {
                            firestore.collection("users").document(userId)
                                .update(updateData)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                                    tvProfileEmail.text = updatedEmail
                                    bottomSheetDialog.dismiss()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Firestore update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Email update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Re-authentication failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        bottomSheetDialog.show()
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val name = document.getString("name") ?: "User"
                val email = document.getString("email") ?: "No Email"
                val role = document.getString("role") ?: "User"
                val photoUrl = document.getString("photoUrl")
                if (!photoUrl.isNullOrEmpty()) {
                    Glide.with(this).load(photoUrl).into(ivProfilePicture)
                }
                tvProfileName.text = name
                tvProfileEmail.text = email
                tvProfileRole.text = "Role: $role"
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }
    private fun uploadImageToCloudinary(imageUri: Uri) {
        val stream = requireContext().contentResolver.openInputStream(imageUri) ?: return
        val requestBody = stream.readBytes().toRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "profile.jpg", requestBody)
            .addFormDataPart("upload_preset", "profileImage")
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/doturqykw/image/upload")
            .post(body)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    if (!isAdded) return@runOnUiThread
                    Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                val imageUrl = try {
                    JSONObject(responseData).getString("secure_url")
                } catch (e: Exception) {
                    null
                }

                if (imageUrl == null) {
                    activity?.runOnUiThread {
                        if (!isAdded) return@runOnUiThread
                        Toast.makeText(requireContext(), "Failed to parse image URL", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                firestore.collection("users").document(userId)
                    .update("photoUrl", imageUrl)
                    .addOnSuccessListener {
                        activity?.runOnUiThread {
                            if (!isAdded) return@runOnUiThread
                            Glide.with(requireContext()).load(imageUrl).into(ivProfilePicture)
                            Toast.makeText(requireContext(), "Profile photo updated", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        activity?.runOnUiThread {
                            if (!isAdded) return@runOnUiThread
                            Toast.makeText(requireContext(), "Firestore update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        })
    }


}
