package com.viteats.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

enum class MealType(
    val displayName: String,
    val timeRange: String,
    val startMinutes: Int,
    val endMinutes: Int,
    val sessionNo: Int
) {
    BREAKFAST("Breakfast", "07:00 AM - 09:30 AM", 7 * 60, 9 * 60 + 30, 1),
    LUNCH("Lunch", "12:00 PM - 02:30 PM", 12 * 60, 14 * 60 + 30, 2),
    SNACKS("Snacks", "05:00 PM - 06:30 PM", 17 * 60, 18 * 60 + 30, 3),
    DINNER("Dinner", "07:00 PM - 09:00 PM", 19 * 60, 21 * 60, 4);

    companion object {
        fun allMeals() = values().toList()
    }
}

data class MealStatus(
    val activeMeal: MealType?,
    val nextMeal: MealType,
    val minutesUntilNext: Int,
    val currentTimeString: String,
    val isMessOpen: Boolean
)

object MealPeriodHelper {
    private fun getIndiaCalendar(): Calendar {
        return Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
    }

    fun getCurrentMealStatus(): MealStatus {
        val calendar = getIndiaCalendar()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val currentMinutes = hour * 60 + minute

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        }
        val currentTimeString = timeFormat.format(calendar.time)

        val activeMeal = MealType.allMeals().find { meal ->
            currentMinutes in meal.startMinutes..meal.endMinutes
        }

        val (nextMeal, minutesUntilNext) = if (activeMeal != null) {
            // Next meal is the following meal
            val nextIndex = (activeMeal.ordinal + 1) % MealType.allMeals().size
            val next = MealType.allMeals()[nextIndex]
            val mins = if (next.startMinutes > currentMinutes) {
                next.startMinutes - currentMinutes
            } else {
                (24 * 60 - currentMinutes) + next.startMinutes
            }
            Pair(next, mins)
        } else {
            // Find next upcoming meal today
            val upcoming = MealType.allMeals().firstOrNull { it.startMinutes > currentMinutes }
            if (upcoming != null) {
                Pair(upcoming, upcoming.startMinutes - currentMinutes)
            } else {
                // Next is Breakfast tomorrow
                val first = MealType.BREAKFAST
                val mins = (24 * 60 - currentMinutes) + first.startMinutes
                Pair(first, mins)
            }
        }

        return MealStatus(
            activeMeal = activeMeal,
            nextMeal = nextMeal,
            minutesUntilNext = minutesUntilNext,
            currentTimeString = currentTimeString,
            isMessOpen = activeMeal != null
        )
    }

    fun formatDuration(minutes: Int): String {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        return when {
            hours > 0 && remainingMinutes > 0 -> "${hours}h ${remainingMinutes}m"
            hours > 0 -> "${hours}h"
            else -> "${remainingMinutes}m"
        }
    }
}
