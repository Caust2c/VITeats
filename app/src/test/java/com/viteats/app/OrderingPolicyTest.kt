package com.viteats.app

import com.viteats.app.data.OrderingPolicy
import com.viteats.app.data.model.CartItem
import com.viteats.app.data.remote.MenuItem
import org.junit.Assert.*
import org.junit.Test

class OrderingPolicyTest {
    private fun item(id: Int, stock: Int = 10, price: Double = 25.0) =
        MenuItem(id, "Food $id", price, "Dinner", 2, "Food $id", "Meals", stock, 1, null, null)

    @Test fun completeOrderRequiresEveryFavourite() {
        assertThrows(IllegalArgumentException::class.java) {
            OrderingPolicy.favourites(setOf(1, 2), listOf(item(1), item(2, 0)), false)
        }
    }

    @Test fun partialOrderUsesOnlyAvailableFavourites() {
        val result = OrderingPolicy.favourites(setOf(1, 2, 3), listOf(item(1), item(2, 0), item(4)), true)
        assertEquals(listOf(1), result.map { it.item.meitid })
        assertEquals(1, result.single().quantity)
    }

    @Test fun emptyPartialOrderCannotBePaid() {
        assertThrows(IllegalArgumentException::class.java) {
            OrderingPolicy.favourites(setOf(1), emptyList(), true)
        }
        assertThrows(IllegalArgumentException::class.java) {
            OrderingPolicy.favourites(emptySet(), listOf(item(1)), false)
        }
    }

    @Test fun reorderKeepsIdentityAndQuantityButUsesCurrentPrice() {
        val result = OrderingPolicy.reorder(listOf(CartItem(item(1), 3), CartItem(item(2), 2)),
            listOf(item(1, price = 30.0), item(2, price = 40.0)))
        assertEquals(listOf(3, 2), result.map { it.quantity })
        assertEquals(170.0, result.sumOf { it.lineTotal }, 0.001)
    }

    @Test fun reorderDoesNotInventUnavailableItems() {
        assertThrows(IllegalArgumentException::class.java) {
            OrderingPolicy.reorder(listOf(CartItem(item(1), 3)), listOf(item(1, 2)))
        }
        assertThrows(IllegalArgumentException::class.java) {
            OrderingPolicy.reorder(listOf(CartItem(item(1))), listOf(item(2)))
        }
        assertThrows(IllegalArgumentException::class.java) { OrderingPolicy.reorder(emptyList(), listOf(item(1))) }
    }

    @Test fun budgetBoundaryAllowsEqualAndDisabledLimit() {
        assertFalse(OrderingPolicy.exceedsLimit(100.0, 100.0))
        assertTrue(OrderingPolicy.exceedsLimit(100.01, 100.0))
        assertFalse(OrderingPolicy.exceedsLimit(500.0, 0.0))
    }
}
