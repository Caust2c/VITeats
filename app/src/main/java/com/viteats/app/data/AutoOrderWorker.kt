package com.viteats.app.data

import android.content.Context
import androidx.work.*
import com.viteats.app.VITeatsApplication
import com.viteats.app.data.local.AutoOrderAttempt
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar
import java.util.concurrent.TimeUnit

object AutoOrderScheduler {
    fun update(context: Context, owner: String, config: AutoOrderConfig) {
        WorkManager.getInstance(context).cancelAllWorkByTag("auto-order-$owner")
        if (config.enabled) enqueueNext(context, owner, config)
    }

    fun enqueueNext(context: Context, owner: String, config: AutoOrderConfig) {
        val now = indiaCalendar()
        val target = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, config.hour)
            set(Calendar.MINUTE, config.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_MONTH, 1)
        }
        val request = OneTimeWorkRequestBuilder<AutoOrderWorker>()
            .setInputData(workDataOf("owner" to owner, "generation" to config.generation,
                "due" to target.timeInMillis))
            .setInitialDelay(target.timeInMillis - now.timeInMillis, TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .addTag("auto-order-$owner")
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork("auto-order-$owner-${target.timeInMillis}",
            ExistingWorkPolicy.KEEP, request)
    }
}

class AutoOrderWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        val app = applicationContext as VITeatsApplication
        val owner = inputData.getString("owner") ?: return Result.success()
        val config = app.autoOrderSettings.read(owner)
        if (!config.enabled || config.generation != inputData.getLong("generation", -1)) return Result.success()
        AutoOrderScheduler.enqueueNext(applicationContext, owner, config)
        fun report(message: String) { app.autoOrderSettings.report(owner, message) }
        if (app.sessionManager.userIdentifier != owner || !app.sessionManager.hasValidSession()) {
            report("Skipped: sign in to enable automatic ordering.")
            return Result.success()
        }
        val due = inputData.getLong("due", 0)
        val age = System.currentTimeMillis() - due
        if (age !in 0..TimeUnit.MINUTES.toMillis(15)) {
            report("Skipped: scheduled time passed by more than 15 minutes.")
            return Result.success()
        }
        try {
            val pin = app.sessionManager.cachedPin ?: error("Sign in again to save your ordering PIN.")
            val items = OrderingPolicy.favourites(app.favouritesManager.favouriteItemIds.value,
                app.menuRepository.getMenuItems(), config.allowPartial)
            require(!OrderingPolicy.exceedsLimit(items.sumOf { it.lineTotal }, config.hardLimit)) {
                "Skipped: favourites exceed your hard budget limit."
            }
            // Claim before any payment request. WorkManager retries/process restarts cannot charge twice.
            val date = SimpleDateFormat("yyyyMMdd", Locale.US).apply { timeZone = indiaCalendar().timeZone }
                .format(java.util.Date(due))
            if (app.orderDatabase.orders().claim(AutoOrderAttempt(owner, date)) == -1L) return Result.success()
            val current = app.autoOrderSettings.read(owner)
            if (!current.enabled || current.generation != config.generation || app.sessionManager.userIdentifier != owner) return Result.success()
            val outcome = app.orderRepository.checkoutOrder(items, pin)
            report(outcome.fold(
                onSuccess = { "Ordered #$it: " + items.joinToString { line -> "${line.item.meitdes} ×${line.quantity}" } },
                onFailure = { "Order not confirmed: ${it.message}. Check history and wallet before retrying." }
            ))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            report(e.message ?: "Automatic order failed. Check your history and wallet.")
        }
        // Do not retry a financial operation after an ambiguous network response.
        return Result.success()
    }
}
