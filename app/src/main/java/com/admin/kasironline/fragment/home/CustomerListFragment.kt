package com.admin.kasironline.fragment.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.admin.kasironline.R
import com.admin.kasironline.adapter.CustomerListAdapter
import com.admin.kasironline.model.CustomerModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_tab_item.*

class CustomerListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tab_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepare()
        checkClass()
    }

    private fun prepare() {
        tv_not_data.text = getString(R.string.tv_not_customer_list)

        rv_data_list?.layoutManager = LinearLayoutManager(context)
        rv_data_list?.setHasFixedSize(true)

        swipe_refresh?.setOnRefreshListener {
            checkClass()
        }
    }

    private fun requestData() {
        val query = FirebaseFirestore.getInstance()
            .collection("customer")

        val options = FirestoreRecyclerOptions.Builder<CustomerModel>()
            .setQuery(query, CustomerModel::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = context?.let { CustomerListAdapter(options) }
        rv_data_list?.adapter = adapter
    }

    private fun checkClass() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("customer")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot?.isEmpty == true) {
                    tv_not_data?.visibility = View.VISIBLE
                    rv_data_list?.visibility = View.GONE
                } else {
                    tv_not_data?.visibility = View.GONE
                    rv_data_list?.visibility = View.VISIBLE
                    requestData()
                }

                swipe_refresh?.isRefreshing = false
            }
    }
}