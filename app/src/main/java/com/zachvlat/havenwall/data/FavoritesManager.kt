package com.zachvlat.havenwall.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FavoritesManager(context: Context) {

    private val prefs = context.getSharedPreferences("havenwall_favorites", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "favorites"

    fun getFavorites(): List<Wallpaper> {
        val json = prefs.getString(key, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Wallpaper>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun isFavorite(id: String): Boolean = getFavorites().any { it.id == id }

    fun toggleFavorite(wallpaper: Wallpaper) {
        val current = getFavorites().toMutableList()
        if (current.none { it.id == wallpaper.id }) {
            current.add(wallpaper)
        } else {
            current.removeAll { it.id == wallpaper.id }
        }
        save(current)
    }

    private fun save(list: List<Wallpaper>) {
        prefs.edit().putString(key, gson.toJson(list)).apply()
    }
}