package com.example.pulsesync.user_manage

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.widget.EditText
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsesync.R
import com.example.pulsesync.user_manage.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UsersManagement : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var searchUserEditText: EditText
    private lateinit var addUserFAB: FloatingActionButton
    private lateinit var userFilterTabs: TabLayout

    private val userList = mutableListOf<User>()
    private val filteredList = mutableListOf<User>()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_management)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        val toolbarUser = findViewById<MaterialToolbar>(R.id.toolbarUser)
        toolbarUser.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        recyclerView = findViewById(R.id.userRecyclerView)
        searchUserEditText = findViewById(R.id.searchUserEditText)
        addUserFAB = findViewById(R.id.addUserFAB)
        userFilterTabs = findViewById(R.id.userTabs)

        toolbarUser.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        recyclerView.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(
            context = this,
            userList = filteredList,
            onUserUpdated = {
                fetchUsersFromFirestore()
            }
        )

        recyclerView.adapter = userAdapter

        fetchUsersFromFirestore()

        searchUserEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterUsers(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        addUserFAB.setOnClickListener {
            openAddUserBottomSheet()
        }

        userFilterTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filterByRole(tab?.text.toString())
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun openAddUserBottomSheet() {
        val bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_dialog, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetView)

        val etUserName = bottomSheetView.findViewById<TextInputEditText>(R.id.etUserName)
        val etUserEmail = bottomSheetView.findViewById<TextInputEditText>(R.id.etUserEmail)
        val etUserRole = bottomSheetView.findViewById<TextInputEditText>(R.id.etUserRole)
        val btnAddUser = bottomSheetView.findViewById<MaterialButton>(R.id.btnAddUser)

        btnAddUser.setOnClickListener {
            val name = etUserName.text.toString().trim()
            val email = etUserEmail.text.toString().trim()
            val role = etUserRole.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || role.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newUser = User(name = name, email = email, role = role, profileUrl = "")
            addUserToFirestore(newUser)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun addUserToFirestore(user: User) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users").document(currentUserUid).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role")
                if (role == "Admin") {
                    val newUserRef = firestore.collection("users").document()
                    val newUser = user.copy(uid = newUserRef.id)

                    newUserRef.set(newUser)
                        .addOnSuccessListener {
                            Toast.makeText(this, "User added successfully", Toast.LENGTH_SHORT).show()
                            fetchUsersFromFirestore()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error adding user: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Only admins can add users", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error checking admin status: ", e)
            }
    }


    private fun fetchUsersFromFirestore() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserUid == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }
        firestore.collection("users").document(currentUserUid.toString()).get()
            .addOnSuccessListener { document ->
                val currentUserRole = document.getString("role")
                if (currentUserRole == "Admin") {
                    firestore.collection("users")
                        .get()
                        .addOnSuccessListener { documents ->
                            userList.clear()
                            for (document in documents) {
                                val user = document.toObject(User::class.java).copy(uid = document.id)
                                userList.add(user)
                            }
                            filteredList.clear()
                            filteredList.addAll(userList)
                            userAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreError", "Error fetching users: ", e)
                            Toast.makeText(this, "Error fetching users", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Access Denied", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error fetching user role: ", e)
            }
    }


    private fun filterUsers(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(userList)
        } else {
            filteredList.addAll(userList.filter {
                it.name.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true)
            })
        }
        userAdapter.notifyDataSetChanged()
    }

    private fun filterByRole(role: String) {
        filteredList.clear()
        if (role == "All") {
            filteredList.addAll(userList)
        } else {
            filteredList.addAll(userList.filter { it.role == role })
        }
        userAdapter.notifyDataSetChanged()
    }
}