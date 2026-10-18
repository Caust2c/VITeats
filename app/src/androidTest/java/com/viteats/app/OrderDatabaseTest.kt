package com.viteats.app

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.viteats.app.data.local.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class OrderDatabaseTest {
    @Test fun refreshKeepsExactBasketAndRawQrAndSeparatesAccounts() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, OrderDatabase::class.java).build()
        try {
            val dao = db.orders()
            val basket = "[{\"quantity\":3}]"
            val rawQr = "\"data:image/png;base64,QUJD\\nREVG\""
            dao.merge(SavedOrder("A", "1", "{\"Status\":\"Success\"}", basket), false)
            dao.saveQr("A", "1", rawQr)
            dao.merge(SavedOrder("A", "1", "{\"Status\":\"Delivered\"}"), true)
            val saved = dao.find("A", "1")!!
            assertEquals(basket, saved.itemsJson)
            assertEquals(rawQr, saved.qrBase64)
            assertTrue(saved.orderJson.contains("Delivered"))
            assertTrue(dao.all("B").isEmpty())
            assertNull(dao.find("B", "1"))
        } finally { db.close() }
    }

    @Test fun scheduledAttemptCanBeClaimedOnlyOncePerAccountPerDay() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, OrderDatabase::class.java).build()
        try {
            val dao = db.orders()
            assertTrue(dao.claim(AutoOrderAttempt("A", "20261010")) != -1L)
            assertEquals(-1L, dao.claim(AutoOrderAttempt("A", "20261010")))
            assertTrue(dao.claim(AutoOrderAttempt("A", "20261011")) != -1L)
            assertTrue(dao.claim(AutoOrderAttempt("B", "20261010")) != -1L)
        } finally { db.close() }
    }
}
