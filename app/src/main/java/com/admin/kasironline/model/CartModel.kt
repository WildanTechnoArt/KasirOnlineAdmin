package com.admin.kasironline.model

data class CartModel(
    var name: String? = null,
    var priceValue: Int? = null,
    var discount: Int? = null,
    var amount: Int? = null,
)