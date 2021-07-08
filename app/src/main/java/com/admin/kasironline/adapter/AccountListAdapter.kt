package com.admin.kasironline.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.admin.kasironline.R
import com.admin.kasironline.model.AccountModel
import com.admin.kasironline.utils.Validation
import com.admin.kasironline.view.ItemView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.account_item.view.*
import kotlinx.android.synthetic.main.add_account_dialog.view.*

class AccountListAdapter(
    options: FirestoreRecyclerOptions<AccountModel>,
    private val listener: ItemView
) :
    FirestoreRecyclerAdapter<AccountModel, AccountListAdapter.ViewHolder>(options) {

    private var alertDialog: AlertDialog? = null
    private lateinit var dialogView: View
    private var mContext: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.account_item, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: AccountModel) {
        val getKey = snapshots.getSnapshot(position).id
        mContext = holder.itemView.context
        val getUsername = item.username.toString()
        val getPassword = item.password.toString()

        holder.apply {
            containerView.tv_username.text = "Username: $getUsername"
            containerView.tv_password.text = "Password: $getPassword"

            containerView.img_more.setOnClickListener {
                val popupMenu = PopupMenu(it.context, it)
                popupMenu.setOnMenuItemClickListener(object :
                    android.widget.PopupMenu.OnMenuItemClickListener,
                    PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(p0: MenuItem?): Boolean {
                        when (p0?.itemId) {
                            R.id.edit_menu -> {
                                editAccountDialog(getUsername, getPassword)
                            }
                            R.id.delete_menu -> {
                                val builder = mContext?.let { it1 ->
                                    MaterialAlertDialogBuilder(it1)
                                        .setTitle("Konfirmasi")
                                        .setMessage("Anda yakin ingin menghapusnya?")
                                        .setPositiveButton("Ya") { _, _ ->
                                            listener.onDelete(getKey)
                                        }
                                        .setNegativeButton("Tidak") { dialog, _ ->
                                            dialog.dismiss()
                                        }
                                }
                                val dialog = builder?.create()
                                dialog?.show()
                            }
                        }
                        return true
                    }
                })
                popupMenu.inflate(R.menu.menu_item)
                popupMenu.show()
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun editAccountDialog(username: String, password: String) {
        val builder = mContext.let { it?.let { it1 -> MaterialAlertDialogBuilder(it1) } }
        dialogView = (mContext as AppCompatActivity).layoutInflater.inflate(
            R.layout.add_account_dialog, null
        )

        (mContext as AppCompatActivity).bindProgressButton(dialogView.btn_add)
        dialogView.btn_add.attachTextChangeAnimator()
        dialogView.btn_add.text = mContext?.getString(R.string.btn_edit)
        dialogView.input_username.setText(username)
        dialogView.input_password.setText(password)

        dialogView.btn_add.setOnClickListener {
            val newUsername = dialogView.input_username.text.toString()
            val newPassword = dialogView.input_password.text.toString()

            if (Validation.validateFields(newUsername) || Validation.validateFields(newPassword)) {
                Toast.makeText(mContext, "Tidak boleh ada data yang kosong", Toast.LENGTH_SHORT)
                    .show()
            } else {
                dialogView.btn_add.showProgress { progressColor = Color.WHITE }
                val db = FirebaseFirestore.getInstance()
                db.collection("users")
                    .document(newUsername)
                    .get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            Toast.makeText(
                                mContext,
                                "Username sudah terdaftar",
                                Toast.LENGTH_SHORT
                            ).show()
                            dialogView.btn_add.hideProgress(R.string.btn_edit)
                        } else {
                            saveAccount(username, newUsername, newPassword)
                        }
                    }.addOnFailureListener {
                        Toast.makeText(mContext, it.localizedMessage, Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }

        builder?.setView(dialogView)
        builder?.setTitle("Edit Kasir")

        alertDialog = builder?.create()
        alertDialog?.show()
    }

    private fun saveAccount(oldUsername: String, username: String, password: String) {
        val data = AccountModel()
        data.username = username
        data.password = password

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(username)
            .set(data)
            .addOnSuccessListener {
                deleteOldUsername(oldUsername)
            }.addOnFailureListener {
                dialogView.btn_add.hideProgress(R.string.btn_edit)
                Toast.makeText(mContext, it.localizedMessage, Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun deleteOldUsername(username: String){
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(username)
            .delete()
            .addOnSuccessListener {
                dialogView.btn_add.hideProgress(R.string.btn_edit)
                alertDialog?.dismiss()
                Toast.makeText(
                    mContext,
                    "Data berhasil diubah",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener {
                dialogView.btn_add.hideProgress(R.string.btn_edit)
                Toast.makeText(mContext, it.localizedMessage, Toast.LENGTH_SHORT)
                    .show()
            }
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}