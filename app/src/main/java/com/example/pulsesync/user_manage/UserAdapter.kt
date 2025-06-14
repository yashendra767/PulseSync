package com.example.pulsesync.user_manage

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsesync.R
import com.example.pulsesync.user_manage.User
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class UserAdapter(private val context : Context, private val userList: MutableList<User>, private val onUserUpdated: () -> Unit) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvUserEmail: TextView = itemView.findViewById(R.id.tvUserEmail)
        val tvUserRole: TextView = itemView.findViewById(R.id.tvUserRole)
        val btnMore: ImageView = itemView.findViewById(R.id.btnMore)

        fun bind(user: User) {
            tvUserName.text = user.name
            tvUserEmail.text = user.email
            tvUserRole.text = user.role
            btnMore.setOnClickListener {
                showManageUserDialog(user)
            }
        }

        private fun showManageUserDialog(user: User) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_manage_user, null)
            val dialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

            dialogView.findViewById<LinearLayout>(R.id.optionEdit).setOnClickListener {
                dialog.dismiss()
                showEditBottomSheet(user)
            }

            dialogView.findViewById<LinearLayout>(R.id.optionDelete).setOnClickListener {
                dialog.dismiss()
                showDeleteConfirmationDialog(user)
            }

            dialog.show()
        }
    }
    private fun showEditBottomSheet(user: User) {
        val bottomSheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_dialog_edit, null)
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(bottomSheetView)

        val etUserName = bottomSheetView.findViewById<TextInputEditText>(R.id.etUserNameEdit)
        val etUserEmail = bottomSheetView.findViewById<TextInputEditText>(R.id.etUserEmailEdit)
        val etUserRole = bottomSheetView.findViewById<TextInputEditText>(R.id.etUserRoleEdit)

        etUserName.setText(user.name)
        etUserEmail.setText(user.email)
        etUserRole.setText(user.role)

        bottomSheetView.findViewById<MaterialButton>(R.id.btnEditUser).setOnClickListener {
            val updatedUser = user.copy(
                name = etUserName.text.toString(),
                email = etUserEmail.text.toString(),
                role = etUserRole.text.toString()
            )
            updateUserInFirestore(updatedUser)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun updateUserInFirestore(user: User) {
        FirebaseFirestore.getInstance().collection("users")
            .document(user.uid)
            .set(user)
            .addOnSuccessListener {
                onUserUpdated()
            }
    }

    private fun showDeleteConfirmationDialog(user: User) {
        val dialog = AlertDialog.Builder(context)
            .setTitle("Delete the User")
            .setMessage("Are you sure you want to deactivate this user?")
            .setPositiveButton("Deactivate") { _, _ ->
                deleteUserFromFirestore(user)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun deleteUserFromFirestore(user: User) {
        FirebaseFirestore.getInstance().collection("users")
            .document(user.uid)
            .delete()
            .addOnSuccessListener {
                onUserUpdated()
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = userList.size
}