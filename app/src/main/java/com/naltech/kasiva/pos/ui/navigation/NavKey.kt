package com.naltech.kasiva.pos.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface NavKey {
    @Serializable
    data object Inventory : NavKey
    
    @Serializable
    data object Categories : NavKey
    
    @Serializable
    data class ProductDetail(val productId: Long?) : NavKey

    @Serializable
    data object POS : NavKey

    @Serializable
    data object Dashboard : NavKey

    @Serializable
    data object Checkout : NavKey

    @Serializable
    data object Transactions : NavKey
}
