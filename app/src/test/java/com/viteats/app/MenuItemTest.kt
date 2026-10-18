package com.viteats.app

import com.google.gson.Gson
import com.viteats.app.data.remote.MenuItem
import org.junit.Assert.assertEquals
import org.junit.Test

class MenuItemTest {
    @Test
    fun imageUrlUsesTheApiProductIdAfterGsonDeserialization() {
        val item = Gson().fromJson("""{"meitid":204,"meitdes":"CHICKEN SUKKA","retrt":120.0,"odtdes":"LUNCH","skuid":1,"dispname":"250G","skudes":"DESIR","StockQty":9,"icat":2}""", MenuItem::class.java)
        assertEquals("https://vit-proodle.expertsoftsys.com/images/204.jpg", item.imageUrl)
        assertEquals("https://vit-proodle.expertsoftsys.com/images/69.jpg", item.copy(meitid = 69).imageUrl)
    }
}
