package com.admin.kasironline.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.admin.kasironline.R
import com.admin.kasironline.model.CustomerModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.customer_item.view.*

class CustomerListAdapter(
    options: FirestoreRecyclerOptions<CustomerModel>
) :
    FirestoreRecyclerAdapter<CustomerModel, CustomerListAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.customer_item, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: CustomerModel) {
        val getUsername = item.name.toString()
        val getPhone = item.phone.toString()

        holder.apply {
            containerView.tv_username.text = "Nama: $getUsername"
            containerView.tv_phone.text = "Nomor Telepon: $getPhone"
        }
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}