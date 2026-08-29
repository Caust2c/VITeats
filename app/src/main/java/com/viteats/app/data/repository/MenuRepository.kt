package com.viteats.app.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.viteats.app.data.SessionManager
import com.viteats.app.data.remote.MenuCategory
import com.viteats.app.data.remote.MenuGroup
import com.viteats.app.data.remote.MenuItem
import com.viteats.app.data.remote.ProodleApi
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class MenuRepository(
    private val api: ProodleApi,
    private val sessionManager: SessionManager
) {
    private val gson = Gson()
    private val tag = "MenuRepository"

    private fun getIndiaCalendar(): Calendar {
        return Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
    }

    private fun getCurrentSessionNo(): Int? {
        val now = getIndiaCalendar()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        val timeInMinutes = hour * 60 + minute

        return when {
            // Breakfast: 07:00 - 09:30
            timeInMinutes in (7 * 60)..(9 * 60 + 30) -> 1
            // Lunch: 12:00 - 14:30
            timeInMinutes in (12 * 60)..(14 * 60 + 30) -> 2
            // Snacks: 17:00 - 18:30
            timeInMinutes in (17 * 60)..(18 * 60 + 30) -> 3
            // Dinner: 19:00 - 21:00
            timeInMinutes in (19 * 60)..(21 * 60 + 0) -> 4
            else -> null
        }
    }

    private fun getDocumentNo(calendar: Calendar): Int {
        // Monday = 1, ..., Sunday = 7
        // java.util.Calendar uses Sunday = 1, Monday = 2...
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1
    }

    private fun getFormattedDate(calendar: Calendar): String {
        return SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(calendar.time)
    }

    private fun getMenuParams(oid: String = "2"): Map<String, String>? {
        val calendar = getIndiaCalendar()
        val sessionNo = getCurrentSessionNo()
        val docNo = getDocumentNo(calendar)
        val dateStr = getFormattedDate(calendar)
        val userId = sessionManager.userIdentifier ?: ""

        if (sessionNo == null) {
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            Log.d(tag, "No active session found for time: $hour:$minute. Showing Mess Closed.")
            return null
        }

        Log.d(tag, "Menu Params: Date=$dateStr, DocumentNo=$docNo, SessionNo=$sessionNo, mobno=$userId, oid=$oid")

        return mapOf(
            "DocumentNo" to docNo.toString(),
            "SessionNo" to sessionNo.toString(),
            "mobno" to userId,
            "flg" to "2",
            "oid" to oid,
            "odt" to dateStr
        )
    }

    suspend fun getCategories(): List<MenuCategory> {
        val params = getMenuParams() ?: return emptyList()
        val response = api.getCategories(params)
        return parseResponse(response)
    }
    
    suspend fun getMenuItems(oid: String = "2"): List<MenuItem> {
        val params = getMenuParams(oid) ?: return emptyList()
        Log.d(tag, "Fetching menu items for oid=$oid")
        val response = api.getMenuItems(params)
        val items = parseResponse<MenuItem>(response)
        Log.d(tag, "Fetched ${items.size} items")
        return items
    }

    private inline fun <reified T> parseResponse(response: Response<String>): List<T> {
        if (!response.isSuccessful) {
            Log.e(tag, "API Error: ${response.code()} ${response.message()}")
            return emptyList()
        }
        val body = response.body() ?: return emptyList()
        
        if (body.trim() == "0") {
            Log.d(tag, "Server returned '0' - Mess likely closed or invalid session")
            return emptyList()
        }
        
        return try {
            val type = object : TypeToken<List<T>>() {}.type
            gson.fromJson(body, type)
        } catch (e: Exception) {
            Log.e(tag, "Parsing error: ${e.message}")
            emptyList()
        }
    }
}
