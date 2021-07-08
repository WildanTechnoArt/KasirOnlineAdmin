package com.admin.kasironline.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.admin.kasironline.R
import com.admin.kasironline.model.SupplierModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.supplier_item.view.*

class SupplierListAdapter(
    options: FirestoreRecyclerOptions<SupplierModel>
) :
    FirestoreRecyclerAdapter<SupplierModel, SupplierListAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.supplier_item, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: SupplierModel) {
        val getUsername = item.supplier.toString()
        val getPhone = item.phone.toString()

        holder.apply {
            containerView.tv_supplier_name.text = "Nama Pemasok: $getUsername"
            containerView.tv_supplier_phone.text = "Nomor Tlp: $getPhone"
        }
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}