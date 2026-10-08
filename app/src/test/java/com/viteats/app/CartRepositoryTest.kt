package com.viteats.app

import com.viteats.app.data.remote.MenuItem
import com.viteats.app.data.repository.CartRepository
import com.viteats.app.util.MealPeriodHelper
import com.viteats.app.util.MealType
import org.junit.Assert.*
import org.junit.Test

class CartRepositoryTest {

    private fun createMenuItem(id: Int, name: String, price: Double, stock: Int = 10): MenuItem {
        return MenuItem(
            meitid = id,
            meitdes = name,
            retrt = price,
            odtdes = "LUNCH",
            skuid = 1,
            dispname = "Standard",
            skudes = "DESIR",
            StockQty = stock,
            icat = 1,
            StartTime = null,
            EndTime = null
        )
    }

    @Test
    fun testAddItemAndQuantity() {
        val repo = CartRepository()
        val item1 = createMenuItem(101, "Chicken Biryani", 150.0)
        val item2 = createMenuItem(102, "Paneer Butter Masala", 120.0)

        assertEquals(0, repo.totalItemCount)
        assertEquals(0.0, repo.totalAmount, 0.001)

        repo.addItem(item1)
        assertEquals(1, repo.totalItemCount)
        assertEquals(150.0, repo.totalAmount, 0.001)
        assertEquals(1, repo.getItemQuantity(101))

        // Add same item again (increment)
        repo.addItem(item1)
        assertEquals(2, repo.totalItemCount)
        assertEquals(300.0, repo.totalAmount, 0.001)
        assertEquals(2, repo.getItemQuantity(101))

        // Add second distinct item
        repo.addItem(item2)
        assertEquals(3, repo.totalItemCount)
        assertEquals(420.0, repo.totalAmount, 0.001)
        assertEquals(2, repo.cartItems.value.size)

        // Decrement item1
        repo.decrementItem(item1)
        assertEquals(1, repo.getItemQuantity(101))
        assertEquals(2, repo.totalItemCount)
        assertEquals(270.0, repo.totalAmount, 0.001)

        // Decrement item1 again (removes from cart)
        repo.decrementItem(item1)
        assertEquals(0, repo.getItemQuantity(101))
        assertEquals(1, repo.totalItemCount)
        assertEquals(120.0, repo.totalAmount, 0.001)

        // Clear cart
        repo.clearCart()
        assertEquals(0, repo.totalItemCount)
        assertEquals(0, repo.cartItems.value.size)
        assertEquals(0.0, repo.totalAmount, 0.001)
    }

    @Test
    fun testAddItemsBatchReorder() {
        val repo = CartRepository()
        val item1 = createMenuItem(201, "Veg Fried Rice", 100.0)
        val item2 = createMenuItem(202, "Fresh Lime Soda", 40.0)

        repo.addItems(listOf(item1 to 2, item2 to 1))
        assertEquals(3, repo.totalItemCount)
        assertEquals(240.0, repo.totalAmount, 0.001)
        assertEquals(2, repo.getItemQuantity(201))
        assertEquals(1, repo.getItemQuantity(202))
    }

    @Test
    fun testMealPeriodHelperFormatting() {
        assertEquals("1h 30m", MealPeriodHelper.formatDuration(90))
        assertEquals("2h", MealPeriodHelper.formatDuration(120))
        assertEquals("45m", MealPeriodHelper.formatDuration(45))
        assertNotNull(MealPeriodHelper.getCurrentMealStatus())
    }

    @Test
    fun testMealTypeSchedule() {
        val meals = MealType.allMeals()
        assertEquals(4, meals.size)
        assertTrue(meals.any { it.displayName == "Breakfast" })
        assertTrue(meals.any { it.displayName == "Lunch" })
        assertTrue(meals.any { it.displayName == "Snacks" })
        assertTrue(meals.any { it.displayName == "Dinner" })
    }
}
