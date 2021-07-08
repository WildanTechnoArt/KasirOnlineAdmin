package com.admin.kasironline.fragment.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.admin.kasironline.R
import com.admin.kasironline.adapter.AccountListAdapter
import com.admin.kasironline.model.AccountModel
import com.admin.kasironline.view.ItemView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_tab_item.*

class AccountListFragment : Fragment(), ItemView {

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
        tv_not_data.text = getString(R.string.tv_not_account_list)

        rv_data_list?.layoutManager = LinearLayoutManager(context)
        rv_data_list?.setHasFixedSize(true)

        swipe_refresh?.setOnRefreshListener {
            checkClass()
        }
    }

    private fun requestData() {
        val query = FirebaseFirestore.getInstance()
            .collection("users")
            .orderBy("username")

        val options = FirestoreRecyclerOptions.Builder<AccountModel>()
            .setQuery(query, AccountModel::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = context?.let { AccountListAdapter(options, this) }
        rv_data_list?.adapter = adapter
    }

    private fun checkClass() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
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

    override fun onDelete(id: String) {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(id)
            .delete()
            .addOnSuccessListener {
                swipe_refresh?.isRefreshing = false
            }.addOnFailureListener {
                swipe_refresh?.isRefreshing = false
                Toast.makeText(context, it.localizedMessage?.toString(), Toast.LENGTH_SHORT).show()
            }
    }
}