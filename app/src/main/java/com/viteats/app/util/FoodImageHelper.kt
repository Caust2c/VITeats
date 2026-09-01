package com.viteats.app.util

import com.viteats.app.data.remote.MenuItem

object FoodImageHelper {

    /**
     * Resolves a high-quality, high-resolution Unsplash food photography image URL
     * matching the menu item dish type, name, and category.
     */
    fun getImageUrl(item: MenuItem): String {
        val name = item.meitdes.lowercase()
        val category = item.skudes.lowercase()

        return when {
            // Chicken Makhani / Butter Chicken / Chicken Curry
            name.contains("chicken") || name.contains("makhani") || name.contains("murgh") ->
                "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?auto=format&fit=crop&w=600&q=80"

            // Plum Cake / Pastry / Bakery / Brownie / Dessert
            name.contains("cake") || name.contains("plum") || name.contains("pastry") || name.contains("brownie") || name.contains("sweet") ->
                "https://images.unsplash.com/photo-1578985545062-69928b1d9587?auto=format&fit=crop&w=600&q=80"

            // Egg Dosa / Masala Dosa / Plain Dosa / South Indian
            name.contains("dosa") || name.contains("uthappam") || name.contains("idli") || name.contains("vada") ->
                "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?auto=format&fit=crop&w=600&q=80"

            // Muskmelon / Cut Fruits / Watermelon / Fruit Salad
            name.contains("muskmelon") || name.contains("melon") || name.contains("fruit") || name.contains("watermelon") ->
                "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?auto=format&fit=crop&w=600&q=80"

            // Biryani / Pulao / Rice
            name.contains("biryani") || name.contains("pulao") || name.contains("fried rice") || name.contains("rice") ->
                "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?auto=format&fit=crop&w=600&q=80"

            // Paneer / Vegetarian Curry / Dal
            name.contains("paneer") || name.contains("dal") || name.contains("curry") || name.contains("kofta") || name.contains("chole") ->
                "https://images.unsplash.com/photo-1631452180519-c014fe946bc7?auto=format&fit=crop&w=600&q=80"

            // Burger / Sandwich / Rolls / Wraps
            name.contains("burger") || name.contains("sandwich") || name.contains("roll") || name.contains("wrap") ->
                "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=600&q=80"

            // Noodles / Pasta / Maggi / Chowmein
            name.contains("noodle") || name.contains("pasta") || name.contains("maggi") || name.contains("chowmein") ->
                "https://images.unsplash.com/photo-1585032226651-759b368d7246?auto=format&fit=crop&w=600&q=80"

            // Tea / Coffee / Chai / Shakes / Beverages / Juice
            name.contains("tea") || name.contains("chai") || name.contains("coffee") || name.contains("shake") || name.contains("juice") || name.contains("lassi") ->
                "https://images.unsplash.com/photo-1551024709-8f23befc6f87?auto=format&fit=crop&w=600&q=80"

            // Paratha / Roti / Naan / Bread
            name.contains("paratha") || name.contains("roti") || name.contains("naan") || name.contains("kulcha") ->
                "https://images.unsplash.com/photo-1626074353765-517a681e40be?auto=format&fit=crop&w=600&q=80"

            // Thali / Full Meals
            category.contains("lunch") || category.contains("dinner") || name.contains("thali") || name.contains("meal") ->
                "https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?auto=format&fit=crop&w=600&q=80"

            // Default fallback food photo
            else -> "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=600&q=80"
        }
    }
}
