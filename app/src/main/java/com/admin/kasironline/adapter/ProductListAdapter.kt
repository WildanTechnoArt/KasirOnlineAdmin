package com.admin.kasironline.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.admin.kasironline.GlideApp
import com.admin.kasironline.R
import com.admin.kasironline.model.ProductModel
import com.admin.kasironline.view.ProductListView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.product_item.view.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class ProductListAdapter(
    options: FirestoreRecyclerOptions<ProductModel>,
    private val listener: ProductListView
) :
    FirestoreRecyclerAdapter<ProductModel, ProductListAdapter.ViewHolder>(options) {

    private var kursIndonesia: DecimalFormat? = null
    private lateinit var mContext: Context

    lateinit var getItemId: String
    private lateinit var getImage: String
    private lateinit var getName: String
    private var getDiscount: Double = 0.0
    private lateinit var getStock: String
    private var getPrice: Double? = 0.0

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
            .inflate(R.layout.product_item, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: ProductModel) {
        mContext = holder.itemView.context
        getItemId = snapshots.getSnapshot(position).id
        getImage = item.image.toString()
        getName = item.name.toString()
        getDiscount = item.discount?.toDouble() ?: 0.0
        getStock = item.stock.toString()
        getPrice = item.priceValue?.times((getDiscount.div(100.0)))?.let {
            item.priceValue?.minus(it)
        }

        holder.apply {
            GlideApp.with(mContext)
                .load(getImage)
                .into(containerView.img_item_product)

            containerView.tv_item_product_name.text = getName
            containerView.tv_item_product_name.isSelected = true
            containerView.tv_item_price.text = getPrice?.toLong()?.let { kursIndonesia?.format(it) }

            if (getDiscount > 0.0) {
                containerView.ly_discount.visibility = View.VISIBLE
                containerView.tv_discount.text = "${getDiscount.toInt()}%"
            } else {
                containerView.ly_discount.visibility = View.GONE
            }

            containerView.tv_item_stock_value.text = "Stok: $getStock"

            containerView.img_more.setOnClickListener { it1 ->
                val popupMenu = PopupMenu(it1.context, it1)
                popupMenu.setOnMenuItemClickListener(object :
                    android.widget.PopupMenu.OnMenuItemClickListener,
                    PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(p0: MenuItem?): Boolean {
                        when (p0?.itemId) {
                            R.id.edit_menu -> {
                                val data = ProductModel()
                                data.priceValue = item.priceValue
                                data.image = item.image.toString()
                                data.stock = item.stock
                                data.discount = item.discount
                                data.name = item.name.toString()

                                listener.onEditProductDialog(snapshots.getSnapshot(position).id, data)
                            }
                            R.id.delete_menu -> {
                                val builder = mContext.let { it1 ->
                                    MaterialAlertDialogBuilder(it1)
                                        .setTitle("Konfirmasi")
                                        .setMessage("Anda yakin ingin menghapusnya?")
                                        .setPositiveButton("Ya") { _, _ ->
                                            listener.onDelete(snapshots.getSnapshot(position).id)
                                        }
                                        .setNegativeButton("Tidak") { dialog, _ ->
                                            dialog.dismiss()
                                        }
                                }
                                val dialog = builder.create()
                                dialog.show()
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

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}