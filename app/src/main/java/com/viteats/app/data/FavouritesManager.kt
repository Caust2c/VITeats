package com.viteats.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FavouritesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("viteats_favourites", Context.MODE_PRIVATE)

    private val _favouriteItemIds = MutableStateFlow<Set<Int>>(loadFavourites())
    val favouriteItemIds: StateFlow<Set<Int>> = _favouriteItemIds.asStateFlow()

    private fun loadFavourites(): Set<Int> {
        val stringSet = prefs.getStringSet(KEY_FAVOURITES, emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    private fun saveFavourites(set: Set<Int>) {
        val stringSet = set.map { it.toString() }.toSet()
        prefs.edit().putStringSet(KEY_FAVOURITES, stringSet).apply()
    }

    fun isFavourite(meitid: Int): Boolean {
        return _favouriteItemIds.value.contains(meitid)
    }

    fun toggleFavourite(meitid: Int): Boolean {
        var isNowFav = false
        _favouriteItemIds.update { current ->
            val updated = if (current.contains(meitid)) {
                isNowFav = false
                current - meitid
            } else {
                isNowFav = true
                current + meitid
            }
            saveFavourites(updated)
            updated
        }
        return isNowFav
    }

    companion object {
        private const val KEY_FAVOURITES = "key_favourite_item_ids"
    }
}
