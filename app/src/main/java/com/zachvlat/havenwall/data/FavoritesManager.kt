package com.zachvlat.havenwall.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FavoritesManager(context: Context) {

    private val prefs = context.getSharedPreferences("havenwall_favorites", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "favorites"

    private var cache: List<Wallpaper>? = null
    var readFailed = false
        private set

    fun getFavorites(): List<Wallpaper> {
        cache?.let { return it }
        val json = prefs.getString(key, null)
        if (json == null) {
            readFailed = false
            return emptyList<Wallpaper>().also { cache = it }
        }
        val list = try {
            gson.fromJson<List<Wallpaper>>(json, object : TypeToken<List<Wallpaper>>() {}.type)
        } catch (_: Exception) {
            null
        }
        readFailed = list == null
        return (list ?: emptyList()).also { cache = it }
    }

    fun isFavorite(id: String): Boolean = getFavorites().any { it.id == id }

    fun toggleFavorite(wallpaper: Wallpaper): Boolean {
        val current = getFavorites()
        if (readFailed) return false
        val updated = if (current.any { it.id == wallpaper.id }) {
            current.filterNot { it.id == wallpaper.id }
        } else {
            current + wallpaper
        }
        if (!prefs.edit().putString(key, gson.toJson(updated)).commit()) return false
        cache = updated
        return true
    }
}
