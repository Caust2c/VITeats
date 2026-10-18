package com.viteats.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "orders", primaryKeys = ["owner", "orderId"])
data class SavedOrder(
    val owner: String,
    val orderId: String,
    val orderJson: String,
    val itemsJson: String = "[]",
    val qrBase64: String? = null,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "auto_order_attempts", primaryKeys = ["owner", "day"])
data class AutoOrderAttempt(val owner: String, val day: String)

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE owner = :owner ORDER BY savedAt DESC")
    suspend fun all(owner: String): List<SavedOrder>

    @Query("SELECT * FROM orders WHERE owner = :owner ORDER BY savedAt DESC")
    fun observe(owner: String): Flow<List<SavedOrder>>

    @Query("SELECT * FROM orders WHERE owner = :owner AND orderId = :id")
    suspend fun find(owner: String, id: String): SavedOrder?

    @Upsert
    suspend fun save(order: SavedOrder)

    @Query("UPDATE orders SET qrBase64 = :raw WHERE owner = :owner AND orderId = :id")
    suspend fun saveQr(owner: String, id: String, raw: String)

    @Transaction
    suspend fun merge(order: SavedOrder, keepItems: Boolean) {
        val old = find(order.owner, order.orderId)
        save(order.copy(itemsJson = if (keepItems) old?.itemsJson ?: "[]" else order.itemsJson,
            qrBase64 = old?.qrBase64, savedAt = old?.savedAt ?: order.savedAt))
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun claim(attempt: AutoOrderAttempt): Long
}

@Database(entities = [SavedOrder::class, AutoOrderAttempt::class], version = 1, exportSchema = false)
abstract class OrderDatabase : RoomDatabase() {
    abstract fun orders(): OrderDao
}
