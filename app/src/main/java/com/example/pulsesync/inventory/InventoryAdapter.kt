package com.example.pulsesync.inventory

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsesync.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InventoryAdapter(
    private val context: Context,
    private val itemList: MutableList<InventoryItem>,
    private val onItemUpdated: () -> Unit
) : RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    inner class InventoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItemName: TextView = itemView.findViewById(R.id.inventoryItemName)
        val tvQuantity: TextView = itemView.findViewById(R.id.inventoryItemQuantity)
        val tvCategory: TextView = itemView.findViewById(R.id.inventoryItemCategory)
        val tvExpiryDate: TextView = itemView.findViewById(R.id.inventoryItemExpiry)
        val btnMore: ImageView = itemView.findViewById(R.id.inventoryItemOptions)

        fun bind(item: InventoryItem) {
            tvItemName.text = item.itemName
            tvQuantity.text = "Quantity: ${item.quantity}"
            tvCategory.text = item.category
            tvExpiryDate.text = "Expiry: ${item.expiryDate}"

            btnMore.setOnClickListener {
                showEditDeleteOptions(item)
            }
        }

        private fun showEditDeleteOptions(item: InventoryItem) {
            val dialog = androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Manage Item")
                .setItems(arrayOf("Edit", "Delete", "Dispense")) { _, index ->
                    when (index) {
                        0 -> showEditBottomSheet(item)
                        1 -> showDeleteDialogBox(item)
                        2 -> showDispenseBottomSheet(item)
                    }
                }
                .create()
            dialog.show()
        }

        private fun showEditBottomSheet(item: InventoryItem) {
            val bottomSheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_edit_inventory, null)
            val bottomSheetDialog = BottomSheetDialog(context)
            bottomSheetDialog.setContentView(bottomSheetView)

            val etItemName = bottomSheetView.findViewById<TextInputEditText>(R.id.etItemName)
            val etQuantity = bottomSheetView.findViewById<TextInputEditText>(R.id.etItemQuantity)
            val etCategory = bottomSheetView.findViewById<TextInputEditText>(R.id.etItemCategory)
            val etExpiryDate = bottomSheetView.findViewById<TextInputEditText>(R.id.etItemExpiry)
            val btnSave = bottomSheetView.findViewById<View>(R.id.btnEditItem)

            etItemName.setText(item.itemName)
            etQuantity.setText(item.quantity.toString())
            etCategory.setText(item.category)
            etExpiryDate.setText(item.expiryDate)

            btnSave.setOnClickListener {
                val name = etItemName.text.toString().trim()
                val qtyStr = etQuantity.text.toString().trim()
                val category = etCategory.text.toString().trim()
                val expiry = etExpiryDate.text.toString().trim()

                if (name.isEmpty() || qtyStr.isEmpty() || category.isEmpty() || expiry.isEmpty()) {
                    Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val qty = qtyStr.toIntOrNull()
                if (qty == null) {
                    Toast.makeText(context, "Quantity must be a number", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val updatedItem = item.copy(
                    itemName = name,
                    quantity = qty,
                    category = category,
                    expiryDate = expiry
                )
                updateItemInFirestore(updatedItem)
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }

        private fun showDispenseBottomSheet(item: InventoryItem) {
            val bottomSheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_dispense_inventory, null)
            val bottomSheetDialog = BottomSheetDialog(context)
            bottomSheetDialog.setContentView(bottomSheetView)

            val etDispenseQty = bottomSheetView.findViewById<TextInputEditText>(R.id.etDispenseQuantity)
            val etNote = bottomSheetView.findViewById<TextInputEditText>(R.id.etDispenseNote)
            val etDispensedTo = bottomSheetView.findViewById<TextInputEditText>(R.id.etDispensedTo)
            val btnDispense = bottomSheetView.findViewById<View>(R.id.btnDispenseItem)

            btnDispense.setOnClickListener {
                val qtyStr = etDispenseQty.text.toString().trim()
                val note = etNote.text.toString().trim()
                val dispensedTo = etDispensedTo.text.toString().trim()

                val dispenseQty = qtyStr.toIntOrNull()
                if (dispenseQty == null || dispenseQty <= 0) {
                    Toast.makeText(context, "Enter a valid quantity", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (dispensedTo.isEmpty()) {
                    Toast.makeText(context, "Destination is required", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (dispenseQty > item.quantity) {
                    Toast.makeText(context, "Insufficient quantity in stock", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val updatedItem = item.copy(quantity = item.quantity - dispenseQty)
                updateItemInFirestore(updatedItem)
                logDispenseHistory(item, dispenseQty, note, dispensedTo)
                bottomSheetDialog.dismiss()
            }
            bottomSheetDialog.show()
        }



        private fun updateItemInFirestore(item: InventoryItem) {
            FirebaseFirestore.getInstance().collection("inventory")
                .document(item.uid)
                .set(item)
                .addOnSuccessListener {
                    onItemUpdated()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show()
                }
        }

        private fun deleteItem(item: InventoryItem) {
            FirebaseFirestore.getInstance().collection("inventory")
                .document(item.uid)
                .delete()
                .addOnSuccessListener {
                    onItemUpdated()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show()
                }
        }

        private fun showDeleteDialogBox(item: InventoryItem){
            val dialog = AlertDialog.Builder(context)
                .setTitle("Delete the Item")
                .setMessage("Are you sure you want to delete this Item?")
                .setPositiveButton("Delete") { _, _ ->
                    deleteItem(item)
                }
                .setNegativeButton("Cancel", null)
                .create()

            dialog.show()
        }

        private fun logDispenseHistory(
            item: InventoryItem,
            quantityDispensed: Int,
            note: String,
            dispensedTo: String
        ) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val dispensedBy = currentUser?.displayName ?: currentUser?.email ?: "Unknown"

            val history = hashMapOf(
                "itemId" to item.uid,
                "itemName" to item.itemName,
                "quantityDispensed" to quantityDispensed,
                "dispensedBy" to dispensedBy,
                "dispensedTo" to dispensedTo,
                "timestamp" to System.currentTimeMillis(),
                "note" to note
            )

            FirebaseFirestore.getInstance().collection("dispensing_history")
                .add(history)
                .addOnSuccessListener {
                    Toast.makeText(context, "Dispensed successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to log dispensing", Toast.LENGTH_SHORT).show()
                }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.inventory_item, parent, false)
        return InventoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int = itemList.size
}
