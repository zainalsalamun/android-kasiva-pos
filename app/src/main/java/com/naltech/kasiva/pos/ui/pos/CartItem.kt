package com.naltech.kasiva.pos.ui.pos

import com.naltech.kasiva.pos.data.local.entities.ProductEntity

data class CartItem(
    val product: ProductEntity,
    val quantity: Int
) {
    val totalPrice: Double get() = product.price * quantity
}
