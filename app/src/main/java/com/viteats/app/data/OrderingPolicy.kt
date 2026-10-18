package com.viteats.app.data

import com.viteats.app.data.model.CartItem
import com.viteats.app.data.remote.MenuItem

object OrderingPolicy {
    fun exceedsLimit(total: Double, limit: Double): Boolean = limit > 0 && total > limit + 0.000001

    fun favourites(ids: Set<Int>, menu: List<MenuItem>, allowPartial: Boolean): List<CartItem> {
        require(ids.isNotEmpty()) { "No favourites selected." }
        val available = ids.mapNotNull { id -> menu.find { it.meitid == id && it.StockQty > 0 } }
        require(allowPartial || available.size == ids.size) { "Skipped: not all favourites are available." }
        require(available.isNotEmpty()) { "Skipped: no favourites are available." }
        return available.map { CartItem(it, 1) }
    }

    fun reorder(saved: List<CartItem>, menu: List<MenuItem>): List<CartItem> {
        require(saved.isNotEmpty()) { "Exact items are unavailable for this older order." }
        return saved.map { line ->
            val current = menu.find { it.meitid == line.item.meitid && it.skuid == line.item.skuid }
            require(current != null && current.StockQty >= line.quantity && line.quantity > 0 && current.retrt.isFinite() && current.retrt >= 0) {
                "${line.item.meitdes} is unavailable in the required quantity."
            }
            CartItem(current, line.quantity)
        }
    }
}
