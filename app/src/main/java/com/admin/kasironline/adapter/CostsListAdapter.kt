package com.admin.kasironline.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.admin.kasironline.R
import com.admin.kasironline.model.CostsModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.costs_item.view.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

class CostsListAdapter(
    options: FirestoreRecyclerOptions<CostsModel>
) :
    FirestoreRecyclerAdapter<CostsModel, CostsListAdapter.ViewHolder>(options) {

    private var kursIndonesia: DecimalFormat? = null

    init {
        kursIndonesia = DecimalFormat.getCurrencyInstance() as DecimalFormat
        val formatRp = DecimalFormatSymbols()

        formatRp.currencySymbol = "Rp. "
        formatRp.monetaryDecimalSeparator = ','
        formatRp.groupingSeparator = '.'
        kursIndonesia?.decimalFormatSymbols = formatRp
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.costs_item, parent, false)

        return ViewHolder(view)
    }
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: CostsModel) {
        val formatter = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())

        val getInformation = item.information.toString()
        val getDate = formatter.format(item.date)
        val getPriceValue = item.priceValue

        holder.apply {
            containerView.tv_information.text = "Keterangan: $getInformation"
            containerView.tv_date.text = "Tanggal: $getDate"
            containerView.tv_price.text =
                "Nominal: ${getPriceValue?.toLong().let { kursIndonesia?.format(it) }}"
        }
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}