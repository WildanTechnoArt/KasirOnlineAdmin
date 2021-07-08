package com.admin.kasironline.view

import com.admin.kasironline.model.ProductModel

interface ProductListView {
    fun onDelete(id: String)
    fun onEditProductDialog(id: String, data: ProductModel)
}