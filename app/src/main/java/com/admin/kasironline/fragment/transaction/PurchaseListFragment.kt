package com.admin.kasironline.fragment.transaction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.admin.kasironline.R
import com.admin.kasironline.adapter.PurchaseListAdapter
import com.admin.kasironline.model.PurchaseModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_tab_item.*

class PurchaseListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tab_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepare()
        checkData()
    }

    private fun prepare() {
        tv_not_data.text = getString(R.string.no_purchase)

        rv_data_list?.layoutManager = LinearLayoutManager(context)
        rv_data_list?.setHasFixedSize(true)

        swipe_refresh?.setOnRefreshListener {
            checkData()
        }
    }

    private fun requestData() {
        val query = FirebaseFirestore.getInstance()
            .collection("purchase")
            .orderBy("date")

        val options = FirestoreRecyclerOptions.Builder<PurchaseModel>()
            .setQuery(query, PurchaseModel::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = PurchaseListAdapter(options)
        rv_data_list?.adapter = adapter
    }

    private fun checkData() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("purchase")
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