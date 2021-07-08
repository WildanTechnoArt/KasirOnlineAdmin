package com.admin.kasironline.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.admin.kasironline.R
import com.admin.kasironline.adapter.OrderListAdapter
import com.admin.kasironline.model.CartModel
import com.admin.kasironline.utils.Constant.TOTAL_COUNT
import com.admin.kasironline.utils.Constant.TOTAL_PRICE
import com.admin.kasironline.utils.Constant.USER_ID
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_detail_order.*

class DetailOrderActivity : AppCompatActivity() {

    private var phone: String? = null
    private var mTotalCount: String? = null
    private var mTotalPrice: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_order)
        prepare()
        requestData()
    }

    private fun prepare() {
        phone = intent.getStringExtra(USER_ID).toString()
        mTotalCount = intent.getStringExtra(TOTAL_COUNT).toString()
        mTotalPrice = intent.getStringExtra(TOTAL_PRICE).toString()

        tv_total_order.text = mTotalCount
        tv_total_price.text = mTotalPrice

        rv_product?.layoutManager = LinearLayoutManager(this)
        rv_product?.setHasFixedSize(true)

        swipe_refresh?.setOnRefreshListener {
            requestData()
        }
    }

    private fun requestData() {
        val query = FirebaseFirestore.getInstance()
            .collection("sales")
            .document(phone.toString())
            .collection("order")

        val options = FirestoreRecyclerOptions.Builder<CartModel>()
            .setQuery(query, CartModel::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = OrderListAdapter(options)
        rv_product?.adapter = adapter
    }
}