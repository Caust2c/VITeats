package com.viteats.app.data.model

import com.viteats.app.data.remote.MenuItem

data class CartItem(
    val item: MenuItem,
    val quantity: Int = 1
) {
    val lineTotal: Double
        get() = item.retrt * quantity
}
