package com.viteats.app.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.viteats.app.data.remote.MenuCategory
import com.viteats.app.data.remote.MenuGroup
import com.viteats.app.data.remote.MenuItem
import com.viteats.app.data.remote.ProodleApi
import retrofit2.Response

class MenuRepository(private val api: ProodleApi) {
    private val gson = Gson()
    
    private val defaultParams = mapOf(
        "DocumentNo" to "1",
        "SessionNo" to "2",
        "oid" to "1"
    )

    suspend fun getMenuGroups(): List<MenuGroup> {
        val response = api.getMenuGroups()
        return parseResponse(response)
    }
    
    suspend fun getCategories(): List<MenuCategory> {
        val response = api.getCategories(defaultParams)
        return parseResponse(response)
    }
    
    suspend fun getMenuItems(): List<MenuItem> {
        val response = api.getMenuItems(defaultParams)
        return parseResponse(response)
    }

    private inline fun <reified T> parseResponse(response: Response<String>): List<T> {
        if (!response.isSuccessful) return emptyList()
        val body = response.body() ?: return emptyList()
        
        // Handle the "0" case (Mess closed / No data)
        if (body.trim() == "0") return emptyList()
        
        return try {
            val type = object : TypeToken<List<T>>() {}.type
            gson.fromJson(body, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
